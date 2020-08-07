package com.portal.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.CategoryType;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.ViewBy;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;

@Controller
@RequestMapping("apiservice")
public class JournalArticleController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalFolderService journalFolderService;

	private static Logger logger = Logger.getLogger(JournalArticleController.class);

	private List<String> removeDelimeterFromTitle(String articleTitle) {
		Document titleDoc = Jsoup.parse(articleTitle);
		replaceTag(titleDoc.children());
		String[] titleInfo = Jsoup.parse(titleDoc.toString()).text().split("/");
		return removeInvalidString(titleInfo);
	}

	public String removeDelimeterFromContent(String articleContent) {
		int startIndex = articleContent.lastIndexOf("[CDATA[") + 7;
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.lastIndexOf("</p>");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		return first.substring(0, endIndex);
	}

	private String getImageUrl(String articleContent, long imageId) {
		int startIndex = articleContent.indexOf("[CDATA[") + 7;
		int endIndex = articleContent.indexOf("]]");
		imageId = imageId != 2835775 ? imageId != 2833620 ? (imageId - 1) : 8312373 : 8312472;
		if (imageId < 0)
			return "";
		String url = "https://myanmar.gov.mm/image/journal/article?img_id=" + imageId;
		String link = startIndex > 0 && endIndex > 0 ? articleContent.substring(startIndex, endIndex) : "";
		return link.contains("https:") ? link : url;
	}

	private String getAttribute(int index, String content, String remover) {
		if (index < 0)
			return "";

		String remainString = content.substring(index, content.length());
		int start = remainString.indexOf(remover);
		if (start < 0)
			return "";

		String remainString2 = remainString.substring(start, remainString.length());
		int startIndex = remainString2.indexOf("CDATA[") + 6;
		int endIndex = remainString2.indexOf("]]");
		String result = remainString2.substring(startIndex, endIndex);

		if (result.isEmpty()) {
			String remainString3 = content.substring(endIndex, remainString2.length());
			int start2 = remainString3.indexOf(remover);
			String remainString4 = remainString3.substring(start2, remainString3.length());
			int startIndex2 = remainString4.indexOf("CDATA[") + 6;
			int endIndex2 = remainString4.indexOf("]]");

			if (start2 < 0 || startIndex2 < 0 || endIndex2 < 0)
				return "";

			result = remainString4.substring(startIndex2, endIndex2);
		}

		return result.startsWith("/") ? "https://myanmar.gov.mm" + result : result;
	}

	private String getShareLinkForNews(String urlTitle) {
		return "https://myanmar.gov.mm/news-media/news/latest-news/-/asset_publisher/idasset354/content/" + urlTitle.replaceAll("%", "%25");
	}

	private JournalArticle getJournalArticleForLatestNew(JournalArticle journalArticle) {

		/* title, imageurl, location, department, date, content */
		JournalArticle newArticle = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newArticle.setEngTitle(title[0]);
		newArticle.setMynamrTitle(title[1]);

		String imageUrl = "";
		imageUrl = imageUrl.isEmpty() ? getDocumentImage(journalArticle.getContent()) : imageUrl;
		newArticle.setImageUrl(imageUrl.isEmpty() ? getHttpImage(journalArticle.getContent()) : imageUrl);

		String con = dp.ParsingSpan(removeDelimeterFromContent(journalArticle.getContent()));
		newArticle.setContent(ImageSourceChange(con).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "").replaceAll("\\&quot;", ""));

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newArticle.setDisplaydate(resultDateString);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		if (name.equals("News and Media"))
			name = "Ministry of Information";

		newArticle.setEngDepartmentTitle(name);
		newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String content = journalArticle.getContent();
		int index = content.indexOf("location");
		newArticle.setEngLocation(getAttribute(index, content, "en_US"));
		newArticle.setMyanmarLocation(getAttribute(index, content, "my_MM"));
		newArticle.setShareLink(getShareLinkForNews(journalArticle.getUrltitle()));
		return newArticle;
	}

	private String getShareLinkForAnnouncements(String urlTitle) {
		return "https://myanmar.gov.mm/news-media/announcements/-/asset_publisher/idasset291/content/" + urlTitle.replaceAll("%", "%25");
	}

	private JournalArticle getJournalArticleForAnnouncement(JournalArticle journalArticle) {

		/* title, imageurl, location, department, date, content */

		JournalArticle newArticle = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newArticle.setEngTitle(title[0]);
		newArticle.setMynamrTitle(title[1]);

		String imageUrl = "";
		imageUrl = imageUrl.isEmpty() ? getDocumentImage(journalArticle.getContent()) : imageUrl;
		newArticle.setImageUrl(imageUrl.isEmpty() ? getHttpImage2(journalArticle.getContent()) : imageUrl);

		String contentInfo = removeDelimeterFromContent(journalArticle.getContent());
		newArticle.setContent(ImageSourceChangeforanouncement(contentInfo.replaceAll("<span style=\"color:#0000ff;\">", "<span>").replaceAll("<span style=\"color:#050505\">", "<span>").replaceAll("<span style=\"font-size:11.5pt\">", "<span>>")).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newArticle.setDisplaydate(resultDateString);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		if (name.equals("News and Media"))
			name = "Ministry of Information";
		newArticle.setEngDepartmentTitle(name);
		newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String content = journalArticle.getContent();
		int index = content.indexOf("location");
		newArticle.setEngLocation(getAttribute(index, content, "en_US"));
		newArticle.setMyanmarLocation(getAttribute(index, content, "my_MM"));
		newArticle.setShareLink(getShareLinkForAnnouncements(journalArticle.getUrltitle()));
		
		try {
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("dateString!!!!!!!!!!!!!!!!!!!!!!!!!!" + dateString);
			Date date = format.parse(dateString);
			newArticle.setDate(date); //2017-12-19 

		} catch (ParseException e) {
			logger.error("Error: " + e);
		}
		return newArticle;
	}

	public void replaceTag(Elements els) {
		ListIterator<Element> iter = els.listIterator();
		while (iter.hasNext()) {
			Element el = iter.next();
			replaceTag(el.children());
			if (el.parentNode() != null)
				el.replaceWith(new TextNode("/" + el.text().trim() + "/"));
		}
	}

	private String getImageString(String articleContent) {
		int startIndex = articleContent.indexOf("/documents/");
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.indexOf(".jpg");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		return "https://myanmar.gov.mm" + first.substring(0, endIndex);
	}

	private String getEngDownLoadLink(String articleContent) {
		int startIndex = articleContent.lastIndexOf("[CDATA[") - 200;
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.lastIndexOf("</p>");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		String raw = first.substring(0, endIndex);

		int rawStart = raw.indexOf("/documents/");
		if (rawStart < 0)
			return "";
		return "https://myanmar.gov.mm" + raw.substring(rawStart, raw.length());
	}

	private JournalArticle getJournalArticleForNewspaper(JournalArticle journalArticle) {
		/* Image url, title, publish date, publisher, pages, language, download link */
		String myanamrImage, engImage = "";
		JournalArticle newArticle = new JournalArticle();

		String imageUrl = getImageString(journalArticle.getContent());
		myanamrImage = imageUrl.contains(".jpg") ? imageUrl : getImageUrl(journalArticle.getContent(), journalArticle.getId_() != 52071110 ? journalArticle.getId_() != 52426493 ? journalArticle.getId_() + 4 : journalArticle.getId_() + 3 : journalArticle.getId_() + 6);
		engImage = imageUrl.contains(".jpg") ? imageUrl : getImageUrl(journalArticle.getContent(), journalArticle.getId_() == 52426493 ? journalArticle.getId_() + 2 : journalArticle.getId_() + 3);

		newArticle.setEngImageUrl(engImage);
		newArticle.setMyanamrImageUrl(myanamrImage);
		newArticle.setId_(journalArticle.getId_());

		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newArticle.setEngTitle(title[0]);
		newArticle.setMynamrTitle(title[1]);

		Document doc = Jsoup.parse(journalArticle.getContent());
		replaceTag(doc.children());
		String[] contentInfo = Jsoup.parse(doc.toString()).text().split("/");
		List<String> stringList = removeInvalidString(contentInfo);
		newArticle.setEngPblisher(stringList.get(0));
		newArticle.setMyanmarPublisher(stringList.get(1));
		newArticle.setPublicationDate(stringList.get(2));
		newArticle.setPage(stringList.get(4));

		newArticle.setEngDownloadLink(getEngDownLoadLink(journalArticle.getContent()));
		newArticle.setMyanmarDownloadLink("https://myanmar.gov.mm" + removeDelimeterFromContent(journalArticle.getContent()));
		newArticle.setContent(journalArticle.getContent());
		return newArticle;
	}

	private String getVideoLink(String content) {
		int start = content.indexOf("https");
		if (start < 0)
			return "";
		String first = content.substring(start, content.length());
		int end = first.indexOf("]]");
		return first.substring(0, end);
	}

	private String getDownloadLink(String content) {
		int start = content.lastIndexOf("[CDATA[") + 7;
		int end = content.lastIndexOf("]]");
		if (start < 0 || end < 0)
			return "";
		return content.substring(start, end).isEmpty() ? "" : "https://myanmar.gov.mm" + content.substring(start, end);
	}

	private String getImageUrl(String content) {
		int start = content.indexOf("/image/");
		int end = content.indexOf("&amp;");
		if (start < 0 || end < 0)
			return "";
		return content.substring(start, end).isEmpty() ? "" : "https://myanmar.gov.mm" + content.substring(start, end);
	}

	private JournalArticle getJournalArticleForMediaVideo(JournalArticle journalArticle) {
		/* photo , video link, download link, department title, date, title */

		JournalArticle newArticle = new JournalArticle();
		List<String> titleList = removeDelimeterFromTitle(journalArticle.getTitle());
		newArticle.setEngTitle(titleList.get(0));
		newArticle.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];

		newArticle.setDisplaydate(resultDateString);
		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		newArticle.setEngDepartmentTitle(name);
		newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());
		newArticle.setVideoLink(getVideoLink(journalArticle.getContent()));
		newArticle.setEngDownloadLink(getDownloadLink(journalArticle.getContent()));

		String engImage = getEngElement(journalArticle.getContent(), "image", "en_US");
		String myaImage = getEngElement(journalArticle.getContent(), "image", "my_MM").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "image", "my_MM") : getEngElement(journalArticle.getContent(), "image", "my_MM");
		engImage = engImage.isEmpty() ? engImage : "https://myanmar.gov.mm" + engImage.substring(engImage.indexOf("/"), engImage.length());
		myaImage = myaImage.isEmpty() ? myaImage : "https://myanmar.gov.mm" + myaImage.substring(myaImage.indexOf("/"), myaImage.length());

		newArticle.setEngImageUrl(engImage);
		newArticle.setMyanamrImageUrl(myaImage);
		newArticle.setContent(journalArticle.getContent());
		return newArticle;
	}

	private JournalArticle parseJournalArticle(JournalArticle journalArticle, CategoryType type) {
		switch (type) {
		case NEWSPAPER:
			return getJournalArticleForNewspaper(journalArticle);
		case MEDIAVIDEO:
			return getJournalArticleForMediaVideo(journalArticle);
		case NEW:
			return getJournalArticleForLatestNew(journalArticle);
		case ANNOUNCEMENT:
			return getJournalArticleForAnnouncement(journalArticle);
		default:
			return getJournalArticleForLatestNew(journalArticle);
		}
	}

	private List<JournalArticle> getJournalArticles(String entryListInfo, CategoryType type) {
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		for (String uuid : entryListInfo.split(",")) {
			if (!DateUtil.isEmpty(uuid)) {
				JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(uuid);
				if (journalArticle != null)
					journalArticles.add(parseJournalArticle(journalArticle, type));
			}
		}
		return journalArticles;
	}

	private JSONObject getJournalArticleByClassTypeIdAndLatest(String input, long classTypeId, CategoryType type) {
		JSONObject resultJson = new JSONObject();
		List<String> entryList = assetEntryService.getAssetEntryListByClassTypeId(classTypeId);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		List<JournalArticle> articles = getJournalArticles(convertEntryListToString(entryList, input), type);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		articles.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < articles.size(); i++) {
			newArticles.add(stackList.pop());
		}

		Collections.sort(newArticles, new Comparator<JournalArticle>() {
			public int compare(JournalArticle o1, JournalArticle o2) {
				if (o1 != null && o2 != null && o2.getDisplaydate() != null && o2.getDisplaydate() != null)
					return o2.getDisplaydate().compareTo(o1.getDisplaydate());
				return 0;
			}
		});

		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", entryList.size());
		return resultJson;
	}
	

	private JSONObject getJournalArticleByClassTypeIdAndLatestAnnouncement(String input, long classTypeId, CategoryType type) {
		JSONObject resultJson = new JSONObject();
		List<String> entryList = assetEntryService.getAssetEntryListByClassTypeId(classTypeId);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		List<JournalArticle> articles = getJournalArticles(convertEntryListToString(entryList, input), type);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		articles.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < articles.size(); i++) {
			newArticles.add(stackList.pop());
		}

		logger.info("announcement!!!!!!!!!!!!!!!!!!!!!!!!!!");
		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", entryList.size());
		return resultJson;
	}


	private JSONObject getJournalArticleByClassTypeIdAndMostView(String input, long classTypeId, CategoryType type) {
		JSONObject resultJson = new JSONObject();
		List<String> entryList = assetEntryService.getAssetEntryListByClassTypeIdAndViewCount(classTypeId);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		List<JournalArticle> articles = getJournalArticles(convertEntryListToString(entryList, input), type);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		articles.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < articles.size(); i++) {
			newArticles.add(stackList.pop());
		}

		Collections.sort(newArticles, new Comparator<JournalArticle>() {
			public int compare(JournalArticle o1, JournalArticle o2) {
				if (o1 != null && o2 != null && o2.getDisplaydate() != null && o2.getDisplaydate() != null)
					return o2.getDisplaydate().compareTo(o1.getDisplaydate());
				return 0;
			}
		});

		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", entryList.size());
		return resultJson;
	}

	@RequestMapping(value = "latestNews", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getLatestNewsByLimit(@RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		// 36205,
		ViewBy viewBy = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewBy) {
		case LATEST:
			return getJournalArticleByClassTypeIdAndLatest(input, 36205, CategoryType.NEW);
		case MOSTVIEW:
			return getJournalArticleByClassTypeIdAndMostView(input, 36205, CategoryType.NEW);
		default:
			return new JSONObject();
		}
	}

	@RequestMapping(value = "announcements", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getAnnouncementsByLimit(@RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		// 36208,
		ViewBy viewBy = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewBy) {
		case LATEST:
			return getJournalArticleByClassTypeIdAndLatestAnnouncement(input, 36208, CategoryType.ANNOUNCEMENT);
		case MOSTVIEW:
			return getJournalArticleByClassTypeIdAndMostView(input, 36208, CategoryType.ANNOUNCEMENT);
		default:
			return new JSONObject();
		}
	}

	@RequestMapping(value = "mediavideos", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getMediaVideosByLimit(@RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		// 36211,
		ViewBy viewBy = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewBy) {
		case LATEST:
			return getJournalArticleByClassTypeIdAndLatest(input, 36211, CategoryType.MEDIAVIDEO);
		case MOSTVIEW:
			return getJournalArticleByClassTypeIdAndMostView(input, 36211, CategoryType.MEDIAVIDEO);
		default:
			return new JSONObject();
		}
	}

	@RequestMapping(value = "newspapers", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getNewspaperByLimit(@RequestParam("input") String input) {
		// 86242,
		return getJournalArticleByClassTypeIdAndLatest(input, 86242, CategoryType.NEWSPAPER);
	}

	public String convertLongListToString(List<Long> articleIdList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (articleIdList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++)
			info += articleIdList.get(i) + ",";
		return info;
	}

	private List<JournalArticle> getResultList(List<String> entryList, String input, CategoryType type, String searchterm) {

		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<JournalArticle> articles = getJournalArticles(convertEntryListToString(entryList, input), type);
		for (JournalArticle journalArticle : articles) {
			StringBuilder searchterms = new StringBuilder();
			if (journalArticle.getShareLink() != null)
				searchterms.append(journalArticle.getShareLink());
			if (journalArticle.getMyanmarLocation() != null)
				searchterms.append(journalArticle.getMyanmarLocation());
			if (journalArticle.getEngLocation() != null)
				searchterms.append(journalArticle.getEngLocation());
			if (journalArticle.getDepartmentTitle() != null)
				searchterms.append(journalArticle.getDepartmentTitle());
			if (journalArticle.getMyanmarDepartmentTitle() != null)
				searchterms.append(journalArticle.getMyanmarDepartmentTitle());
			if (journalArticle.getEngDepartmentTitle() != null)
				searchterms.append(journalArticle.getEngDepartmentTitle());
			if (journalArticle.getMynamrTitle() != null)
				searchterms.append(journalArticle.getMynamrTitle());
			if (journalArticle.getEngTitle() != null)
				searchterms.append(journalArticle.getEngTitle());
			if (journalArticle.getDisplaydate() != null)
				searchterms.append(journalArticle.getDisplaydate());
			if (journalArticle.getContent() != null)
				searchterms.append(journalArticle.getContent());
			if (journalArticle.getImageUrl() != null)
				searchterms.append(journalArticle.getImageUrl());
			if (journalArticle.getEngPblisher() != null)
				searchterms.append(journalArticle.getEngPblisher());
			if (journalArticle.getMyanmarPublisher() != null)
				searchterms.append(journalArticle.getMyanmarPublisher());
			if (journalArticle.getPublicationDate() != null)
				searchterms.append(journalArticle.getPublicationDate());
			if (searchterms.toString().contains(searchterm))
				resultList.add(journalArticle);
		}
		return resultList;

	}

	private JSONObject byClassTypeIdAndSearchTerms(String searchterm, long classTypeId, CategoryType type, String input) {
		JSONObject resultJson = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<String> entryList = assetEntryService.getAssetEntryListByClassTypeId(classTypeId);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		while (resultList.size() < 10 && Integer.parseInt(input) < lastPageNo) {
			resultList.addAll(getResultList(entryList, input, type, searchterm));
			input = (Integer.parseInt(input) + 1) + "";
		}
		resultJson.put("articles", resultList);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", journalArticleService.getAllBySearchterm(searchterm, classTypeId));
		resultJson.put("lastInput", input);
		return resultJson;
	}

	@RequestMapping(value = "bysearchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getPolls(@RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("categorytype") String categorytype) {
		JSONObject resultJson = new JSONObject();
		CategoryType categoryType = CategoryType.valueOf(categorytype.toUpperCase().trim());
		switch (categoryType) {
		case NEW:
			return byClassTypeIdAndSearchTerms(searchterm, 36205, CategoryType.NEW, input);
		case ANNOUNCEMENT:
			return byClassTypeIdAndSearchTerms(searchterm, 36208, CategoryType.ANNOUNCEMENT, input);
		case MEDIAVIDEO:
			return byClassTypeIdAndSearchTerms(searchterm, 36211, CategoryType.MEDIAVIDEO, input);
		case NEWSPAPER:
			return byClassTypeIdAndSearchTerms(searchterm, 86242, CategoryType.NEWSPAPER, input);
		default:
			return resultJson;
		}
	}
}
