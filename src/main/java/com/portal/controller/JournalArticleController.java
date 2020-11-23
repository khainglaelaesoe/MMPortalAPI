package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
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

		String image = dp.parseImageForLatestNews(journalArticle.getContent());
		newArticle.setImageUrl(image);

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newArticle.setDisplaydate(resultDateString);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		if (name.equals("News and Media"))
			name = "Ministry of Information";

		if (name.equals("Ministry of Social Welfare, Relief and Resettlement"))
			name = "Ministry of Social Welfare Relief Resettlement";

		if (name.equals("Constitutional Tribunal of the Union of Myanmar"))
			name = "Constitutional Tribunal";

		newArticle.setEngDepartmentTitle(name);
		newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String content = journalArticle.getContent();
		int index = content.indexOf("location");
		newArticle.setEngLocation(getAttribute(index, content, "en_US"));
		newArticle.setMyanmarLocation(getAttribute(index, content, "my_MM"));
		newArticle.setShareLink(getShareLinkForNews(journalArticle.getUrltitle()));

		String engContent = getEngElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"en_US\">");
		String myaContent = getEngElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"my_MM\">");

		engContent = engContent.isEmpty() ? myaContent : engContent;
		myaContent = myaContent.isEmpty() ? engContent : myaContent;

		newArticle.setEngContent(engContent.isEmpty() ? "" : ImageSourceChange(dp.ParsingSpan(engContent)).replaceAll("color:#333333", "").replaceAll("background-color:#ffffff", ""));
		newArticle.setMyanmarContent(myaContent.isEmpty() ? "" : ImageSourceChange(dp.ParsingSpan(myaContent)).replaceAll("color:#333333", "").replaceAll("background-color:#ffffff", ""));

		newArticle.setContent(journalArticle.getContent());
		newArticle.setCategoryType(CategoryType.NEW);
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

	private JournalArticle getJournalArticleForNewspaper(JournalArticle journalArticle) {
		/* Image url, title, publish date, publisher, pages, language, download link */
		JournalArticle newArticle = new JournalArticle();
		newArticle.setId_(journalArticle.getId_());

		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newArticle.setEngTitle(title[0]);
		newArticle.setMynamrTitle(title[1]);

		String[] engmyanDownloadLink = new DocumentParsing().Parsingdocument_library(journalArticle.getContent());
		newArticle.setEngDownloadLink(engmyanDownloadLink[0]);
		newArticle.setMyanmarDownloadLink(engmyanDownloadLink[1]);
		newArticle.setContent(journalArticle.getContent());

		/* publisher */
		newArticle.setEngPblisher(getEngElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"en_US\">"));
		newArticle.setMyanmarPublisher(getEngElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"my_MM\">"));

		/* date */
		String engDate = getEngElement(journalArticle.getContent(), "PublicationDate", "<dynamic-content language-id=\"en_US\">");
		String myaDate = getEngElement(journalArticle.getContent(), "PublicationDate", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "PublicationDate", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "PublicationDate", "<dynamic-content language-id=\"my_MM\">");
		newArticle.setPublicationDate(engDate.isEmpty() ? myaDate.split("-")[0] : engDate.split("-")[0]);

		/* page */
		newArticle.setEngPage(getEngElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"en_US\">"));
		newArticle.setMyaPage(getEngElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"my_MM\">"));
		newArticle.setPage(newArticle.getEngPage());

		/* image */
		List<String> engImageList = dp.ParsingEngImage2(journalArticle.getContent());
		newArticle.setEngImageUrl(CollectionUtils.isEmpty(engImageList) ? "" : engImageList.get(0));

		List<String> myaImageList = dp.ParsingMyanImage2(journalArticle.getContent());
		newArticle.setMyanamrImageUrl(CollectionUtils.isEmpty(myaImageList) ? "" : myaImageList.get(0));

		/* Language */
		newArticle.setEngLanguage(getEngElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"en_US\">"));
		newArticle.setMyaLanguage(getEngElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"my_MM\">"));
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

	private JSONObject getJournalArticleByClassTypeIdAndLatest(String input, long classTypeId, CategoryType type) {
		JSONObject resultJson = new JSONObject();
		List<Long> classPKs = assetEntryService.getClassPkList(classTypeId);
		int lastPageNo = classPKs.size() % 10 == 0 ? classPKs.size() / 10 : classPKs.size() / 10 + 1;

		List<JournalArticle> articles = getJournalArticles(convertLongListToString(classPKs, input), type);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		articles.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < articles.size(); i++) {
			newArticles.add(stackList.pop());
		}

		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", classPKs.size());
		return resultJson;
	}

	private JSONObject getJournalArticleByClassTypeIdAndMostView(String input, long classTypeId, CategoryType type) {
		JSONObject resultJson = new JSONObject();
		List<Long> classPks = assetEntryService.getClassPKListViewCount(classTypeId);
		int lastPageNo = classPks.size() % 10 == 0 ? classPks.size() / 10 : classPks.size() / 10 + 1;

		List<JournalArticle> articles = getJournalArticles(convertLongListToString(classPks, input), type);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		articles.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < articles.size(); i++) {
			newArticles.add(stackList.pop());
		}

		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", classPks.size());
		return resultJson;
	}

	@RequestMapping(value = "latestNews", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getLatestNewsByLimit(@RequestHeader("Authorization") String encryptedString, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		JSONObject json = new JSONObject();

		if (!isValidPaganation(input)) {
			json.put("status", 0);
			json.put("message", "Page index out of range!");
			return json;
		}

		if (!isValidViewBy(viewby)) {
			json.put("status", 0);
			json.put("message", "Can't view by this order!");
			return json;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				json.put("status", 0);
				json.put("message", "Authorization failure!");
				return json;
			}
		} catch (Exception e) {
			json.put("status", 0);
			json.put("message", "Authorization failure!");
			return json;
		}
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
	public JSONObject getAnnouncementsByLimit(@RequestHeader("Authorization") String encryptedString, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		JSONObject json = new JSONObject();

		if (!isValidPaganation(input)) {
			json.put("status", 0);
			json.put("message", "Page index out of range!");
			return json;
		}

		if (!isValidViewBy(viewby)) {
			json.put("status", 0);
			json.put("message", "Can't view by this order!");
			return json;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				json.put("status", 0);
				json.put("message", "Authorization failure!");
				return json;
			}
		} catch (Exception e) {
			json.put("status", 0);
			json.put("message", "Authorization failure!");
			return json;
		}
		// 36208,
		ViewBy viewBy = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewBy) {
		case LATEST:
			return getJournalArticleByClassTypeIdAndLatest(input, 36208, CategoryType.ANNOUNCEMENT);
		case MOSTVIEW:
			return getJournalArticleByClassTypeIdAndMostView(input, 36208, CategoryType.ANNOUNCEMENT);
		default:
			return new JSONObject();
		}
	}

	@RequestMapping(value = "mediavideos", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getMediaVideosByLimit(@RequestHeader("Authorization") String encryptedString, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		JSONObject json = new JSONObject();

		if (!isValidPaganation(input)) {
			json.put("status", 0);
			json.put("message", "Page index out of range!");
			return json;
		}

		if (!isValidViewBy(viewby)) {
			json.put("status", 0);
			json.put("message", "Can't view by this order!");
			return json;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				json.put("status", 0);
				json.put("message", "Authorization failure!");
				return json;
			}
		} catch (Exception e) {
			json.put("status", 0);
			json.put("message", "Authorization failure!");
			return json;
		}
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
	public JSONObject getNewspaperByLimit(@RequestHeader("Authorization") String encryptedString, @RequestParam("input") String input) {
		JSONObject json = new JSONObject();

		if (!isValidPaganation(input)) {
			json.put("status", 0);
			json.put("message", "Page index out of range!");
			return json;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				json.put("status", 0);
				json.put("message", "Authorization failure!");
				return json;
			}
		} catch (Exception e) {
			json.put("status", 0);
			json.put("message", "Authorization failure!");
			return json;
		}
		// 86242,
		return getJournalArticleByClassTypeIdAndLatest(input, 86242, CategoryType.NEWSPAPER);
	}

	private List<JournalArticle> getResultList(List<Long> classPks, String input, CategoryType type, String searchterm) {

		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<JournalArticle> articles = getJournalArticles(convertLongListToString(classPks, input), type);
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

	private List<JournalArticle> getJournalArticles(String classPkInfo, CategoryType type) {
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		for (String classpk : classPkInfo.split(",")) {
			if (!classpk.isEmpty()) {
				JournalArticle journalArticle = journalArticleService.byClassPK(Long.parseLong(classpk));
				if (journalArticle != null)
					journalArticles.add(parseJournalArticle(journalArticle, type));
			}
		}
		return journalArticles;
	}

	private List<JournalArticle> parseArticlesWithPaganation(String articleInfo, CategoryType type) {
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		String[] articleList = articleInfo.split(",");
		for (String articleId : articleList) {
			JournalArticle journalArticle = journalArticleService.getMaxVersionJournalByArticleId(articleId);
			if (journalArticle != null)
				journalArticles.add(parseJournalArticle(journalArticle, type));
		}
		return journalArticles;
	}

	private JSONObject getArticlesBySearchTerms(String searchterm, long classTypeId, CategoryType type, String input) {
		JSONObject resultJson = new JSONObject();

		List<String> articleList = journalArticleService.byClassPKAndSearchTerm(classTypeId, searchterm);
		List<JournalArticle> resultList = parseArticlesWithPaganation(byPaganationWithArticle(articleList, input), type);

		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		resultList.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < resultList.size(); i++) {
			newArticles.add(stackList.pop());
		}

		int lastPageNo = articleList.size() % 10 == 0 ? articleList.size() / 10 : articleList.size() / 10 + 1;

		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", 0);
		resultJson.put("lastInput", Integer.parseInt(input));
		return resultJson;
	}

	@RequestMapping(value = "overallsearch", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject overallsearch(@RequestHeader("Authorization") String encryptedString, @RequestParam("searchterm") String searchTerm, @RequestParam("input") String input) {
		JSONObject resultJson = new JSONObject();

		if (!isValidPaganation(input)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Page index out of range!");
			return resultJson;
		}

		if (!isValidSearchTerm(searchTerm)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Topic is not found!");
			return resultJson;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		List<Long> resourcePrimKeys = journalArticleService.getJournalsByOverallSearch(searchTerm);
		if (CollectionUtils.isEmpty(resourcePrimKeys)) {
			resultJson.put("articles", new ArrayList<JournalArticle>());
			resultJson.put("lastPageNo", 0);
			return resultJson;
		}

		for (Long resourcePrimKey : resourcePrimKeys) {
			Long classtypeid = assetEntryService.getClassTypeId(resourcePrimKey);
			JournalArticle journalArticle = journalArticleService.getJournalArticleByResourcePrimKey(resourcePrimKey);
			if (journalArticle != null) {

				switch (classtypeid.toString()) {
				case "36205":
					journalArticles.add(getJournalArticleForLatestNew(journalArticle));
					break;
				case "36208":
					journalArticles.add(getJournalArticleForAnnouncement(journalArticle));
					break;
				}
			}
		}

		int lastPageNo = journalArticles.size() % 10 == 0 ? journalArticles.size() / 10 : journalArticles.size() / 10 + 1;
		resultJson.put("articles", byPaganation(journalArticles, input));
		resultJson.put("lastPageNo", lastPageNo);
		return resultJson;
	}

	@RequestMapping(value = "bysearchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getJournals(@RequestHeader("Authorization") String encryptedString, @RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("categorytype") String categorytype) {
		JSONObject resultJson = new JSONObject();

		if (!isValidPaganation(input)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Page index out of range!");
			return resultJson;
		}

		if (!isValidSearchTerm(searchterm)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Topic is not found!");
			return resultJson;
		}

		if (!isValidCategoryType(categorytype)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Category Type is not valid!");
			return resultJson;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		CategoryType categoryType = CategoryType.valueOf(categorytype.toUpperCase().trim());
		switch (categoryType) {
		case NEW:
			return getArticlesBySearchTerms(searchterm, 36205, CategoryType.NEW, input);
		case ANNOUNCEMENT:
			return getArticlesBySearchTerms(searchterm, 36208, CategoryType.ANNOUNCEMENT, input);
		case MEDIAVIDEO:
			return getArticlesBySearchTerms(searchterm, 36211, CategoryType.MEDIAVIDEO, input);
		case NEWSPAPER:
			return getArticlesBySearchTerms(searchterm, 86242, CategoryType.NEWSPAPER, input);
		default:
			return resultJson;
		}
	}

	@RequestMapping(value = "applink", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getAppLink() {
		JSONObject resultJson = new JSONObject();
		resultJson.put("applink", "https://play.google.com/store/apps/details?id=com.securelink.myangov");
		return resultJson;
	}
}
