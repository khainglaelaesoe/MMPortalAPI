package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.RatingsEntry;
import com.portal.entity.TopicEngName;
import com.portal.entity.ViewBy;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;
import com.portal.service.RatingsEntryService;

@Controller
@RequestMapping("service")
public class ServiceController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalFolderService journalFolderService;

	@Autowired
	private RatingsEntryService ratingsEntryService;

	@Value("${SERVICEURL}")
	private String SERVICEURL;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	public void replaceTag(Elements els) {
		ListIterator<Element> iter = els.listIterator();
		while (iter.hasNext()) {
			Element el = iter.next();
			replaceTag(el.children());
			if (el.parentNode() != null)
				el.replaceWith(new TextNode("/" + el.text().trim() + "/"));
		}
	}

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());

		newJournal.setEngTitle(title[0]);
		newJournal.setMynamrTitle(title[1]);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		newJournal.setEngDepartmentTitle(name);
		newJournal.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String engContent = "", myanmarContent = "";
		String content = journalArticle.getContent();
		int begin = content.indexOf("\"text_area\"");
		content = content.substring(begin, content.length());

		int start = content.indexOf("<dynamic-content language-id=\"en_US\">");
		int end = content.indexOf("</dynamic-content>");

		engContent = dp.ParsingSpan(Jsoup.parse(content.substring(start, end)).text().replaceAll("value 1", "")).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "").replaceAll("\\&quot;", "");
		String remainString = content.substring(end, content.length());

		int mStart = remainString.indexOf("<dynamic-content language-id=\"my_MM\">");
		if (mStart > 0) {
			int mEnd = remainString.lastIndexOf("</dynamic-content>");
			myanmarContent = dp.ParsingSpan(Jsoup.parse(remainString.substring(mStart, mEnd)).text().replaceAll("value 1", "")).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "").replaceAll("\\&quot;", "");
		}

		newJournal.setMyanmarContent(!myanmarContent.isEmpty() ? myanmarContent : engContent);
		newJournal.setEngContent(!engContent.isEmpty() ? engContent : myanmarContent);

		newJournal.setEngContent(ImageSourceChangeforanouncement(dp.ParsingSpan(newJournal.getEngContent())));
		newJournal.setMyanmarContent(ImageSourceChangeforanouncement(dp.ParsingSpan(newJournal.getMyanmarContent())));

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newJournal.setDisplaydate(resultDateString);
		newJournal.setId_(journalArticle.getId_());
		newJournal.setRating(journalArticle.getRating());
		newJournal.setRatingAction(journalArticle.getRatingAction());
		newJournal.setShareLink(journalArticle.getShareLink() == null ? "" : journalArticle.getShareLink());
		newJournal.setClassNameString(journalArticle.getClassNameString());
		newJournal.setpKString(journalArticle.getpKString());
		newJournal.setUserRating(journalArticle.getUserRating());
		return newJournal;
	}

	public List<JournalArticle> getJournalArticlesBySearchTerm(List<Long> classPKList, String input, String userId, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		// String info = convertLongListToString(classPKList, input);
		// String[] classpks = info.split(",");
		for (Long classPK : classPKList) {
			String classpk = classPK.toString();
			JournalArticle journalArticle = journalArticleService.byClassPKAndSearchTerms(classPK, searchTerm);
			if (journalArticle != null) {
				Long entryId = assetEntryService.getClassNameByClassUuid(classpk).get(0);
				Long classNameId = assetEntryService.getClassName(classpk).get(0);

				List<RatingsEntry> ratingsEntriesFromWeb = ratingsEntryService.getScoresByClass(classNameId, classPK);
				List<String> mobileRatingsEntries = getRatingsEntry(classNameId.toString(), classpk);

				double totalScore = 0;
				String ratingAction = "no";
				double userRating = 0.0;
				for (RatingsEntry entry : ratingsEntriesFromWeb) {
					totalScore += entry.getScore();

					/* web user id */
					String webUserId = getWebUserId(userId);
					if (webUserId != null && !webUserId.isEmpty() && Long.parseLong(webUserId) == entry.getUserid()) {
						userRating += entry.getScore();
						ratingAction = "yes";
					}
				}

				for (String entryStr : mobileRatingsEntries) {
					String[] strArr = entryStr.split(",");
					totalScore += Double.parseDouble(strArr[1]);
					if (Long.parseLong(userId) == Long.parseLong(strArr[0])) {
						userRating += Double.parseDouble(strArr[1]);
						ratingAction = "yes";
					}
				}

				journalArticle.setRating(CollectionUtils.isEmpty(ratingsEntriesFromWeb) && CollectionUtils.isEmpty(mobileRatingsEntries) ? 0 : totalScore / (ratingsEntriesFromWeb.size() + mobileRatingsEntries.size()));
				journalArticle.setUserRating(userRating);
				journalArticle.setShareLink(getShareLink(journalArticle.getUrltitle(), entryId.toString()));
				journalArticle.setClassNameString(classNameId.toString());
				journalArticle.setpKString(classpk);
				journalArticle.setRatingAction(ratingAction);
				journalArticleList.add(journalArticle);
			}

		}
		return journalArticleList;
	}

	public List<JournalArticle> getArticlesByUserId(List<Long> classpks, String input, String userId) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		String info = convertLongListToString(classpks, input);
		String[] classpkList = info.split(",");
		for (String classpk : classpkList) {
			if (classpk.isEmpty())
				continue;
			Long classPK = Long.parseLong(classpk);
			JournalArticle journalArticle = journalArticleService.byClassPK(classPK);
			if (journalArticle != null) {
				Long entryId = assetEntryService.getClassNameByClassUuid(classpk).get(0);
				Long classNameId = assetEntryService.getClassName(classpk).get(0);

				List<RatingsEntry> ratingsEntriesFromWeb = ratingsEntryService.getScoresByClass(classNameId, classPK);
				List<String> mobileRatingsEntries = getRatingsEntry(classNameId.toString(), classpk);

				double totalScore = 0;
				String ratingAction = "no";
				double userRating = 0.0;
				for (RatingsEntry entry : ratingsEntriesFromWeb) {
					totalScore += entry.getScore();

					/* web user id */
					String webUserId = getWebUserId(userId);
					if (webUserId != null && Long.parseLong(webUserId) == entry.getUserid()) {
						userRating += entry.getScore();
						ratingAction = "yes";
					}
				}

				for (String entryStr : mobileRatingsEntries) {
					String[] strArr = entryStr.split(",");
					totalScore += Double.parseDouble(strArr[1]);
					if (Long.parseLong(userId) == Long.parseLong(strArr[0])) {
						userRating += Double.parseDouble(strArr[1]);
						ratingAction = "yes";
					}
				}

				journalArticle.setRating(CollectionUtils.isEmpty(ratingsEntriesFromWeb) && CollectionUtils.isEmpty(mobileRatingsEntries) ? 0 : totalScore / (ratingsEntriesFromWeb.size() + mobileRatingsEntries.size()));
				journalArticle.setUserRating(userRating);
				journalArticle.setShareLink(getShareLink(journalArticle.getUrltitle(), entryId.toString()) == null ? "" : getShareLink(journalArticle.getUrltitle(), entryId.toString()));
				journalArticle.setClassNameString(classNameId.toString());
				journalArticle.setpKString(classpk);
				journalArticle.setRatingAction(ratingAction);
				journalArticleList.add(journalArticle);
			}

		}
		return journalArticleList;
	}

	private String getShareLink(String urlTitle, String entryId) {
		return "https://myanmar.gov.mm/services/-/asset_publisher/idasset450/content/" + urlTitle.replaceAll("%", "%25")
		        + "?_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_idasset450_redirect=https%3A%2F%2Fmyanmar.gov.mm%2Fservices%3Fp_p_id%3Dcom_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_idasset450%26p_p_lifecycle%3D0%26p_p_state%3Dnormal%26p_p_mode%3Dview%26_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_idasset450_cur%3D0%26p_r_p_resetCur%3Dfalse%26_com_liferay_asset_publisher_web_portlet_AssetPublisherPortlet_INSTANCE_idasset450_assetEntryId%3D"
		        + entryId;
	}

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	private List<JournalArticle> getResultList(List<Long> classPkList, String input, String searchTerm, String userId) {
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<JournalArticle> journalArticleList = getArticlesByUserId(classPkList, input, userId);
		journalArticleList.forEach(journalArticle -> {
			StringBuilder searchTerms = new StringBuilder();
			searchTerms.append(journalArticle.getContent());
			searchTerms.append(journalArticle.getTitle());
			if (searchTerms.toString().contains(searchTerm))
				resultList.add(journalArticle);
		});
		return resultList;
	}

	public List<Long> setValue(long categoryId, String searchTerm) {
		return journalArticleService.getServiceByTopicAndSearchTerm2(categoryId, searchTerm);

	}

	@RequestMapping(value = "searchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getServicesBySearchTerm(@RequestHeader("Authorization") String encryptedString, @RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic, @RequestParam("userid") String userId) {
		JSONObject json = new JSONObject();

		if (!isValidPaganation(input)) {
			json.put("status", 0);
			json.put("message", "Page index out of range!");
			return json;
		}

		if (!isValidTopic(topic)) {
			json.put("status", 0);
			json.put("message", "Topic is not found!");
			return json;
		}

		if (!isValidSearchTerm(searchTerm)) {
			json.put("status", 0);
			json.put("message", "Avoid too many keywords!");
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

		List<Long> classpks = new ArrayList<Long>();

		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85099);
			List<JournalArticle> journals = parseJournalArticleList(getJournalArticlesBySearchTerm(classpks, input, userId, searchTerm));
			int lastPageNo = journals.size() % 10 == 0 ? journals.size() / 10 : journals.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("services", byPaganation(journals, input));
			json.put("totalCount", 0);
			json.put("lastInput", 0);
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks.addAll(setValue(80486, searchTerm));
			break;
		case Education_Research:
			classpks.addAll(setValue(80484, searchTerm));
			break;
		case Social:
			classpks.addAll(setValue(80485, searchTerm));
			break;
		case Economy:
			classpks.addAll(setValue(96793, searchTerm));
			break;
		case Agriculture:
			classpks.addAll(setValue(80491, searchTerm));
			break;
		case Labour_Employment:
			classpks.addAll(setValue(80494, searchTerm));
			break;
		case Livestock:
			classpks.addAll(setValue(87834, searchTerm));
			break;
		case Law_Justice:
			classpks.addAll(setValue(96797, searchTerm));
			break;
		case Security:
			classpks.addAll(setValue(96799, searchTerm));
			break;
		case Hotel_Tourism:
			classpks.addAll(setValue(80488, searchTerm));
			break;
		case Citizen:
			classpks.addAll(setValue(96801, searchTerm));
			break;
		case Natural_Resources_Environment:
			classpks.addAll(setValue(80501, searchTerm));
			break;
		case Industries:
			classpks.addAll(setValue(80495, searchTerm));
			break;
		case Construction:
			classpks.addAll(setValue(96804, searchTerm));

			break;
		case Science:
			classpks.addAll(setValue(80499, searchTerm));

			break;
		case Technology:
			classpks.addAll(setValue(80496, searchTerm));

			break;
		case Transportation:
			classpks.addAll(setValue(97769, searchTerm));

			break;
		case Communication:
			classpks.addAll(setValue(96809, searchTerm));

			break;
		case Information_Media:
			classpks.addAll(setValue(96815, searchTerm));

			break;
		case Religion_Art_Culture:
			classpks.addAll(setValue(80493, searchTerm));

			break;
		case Finance_Tax:
			classpks.addAll(setValue(80489, searchTerm));

			break;
		case SMEs:
			classpks.addAll(setValue(80503, searchTerm));

			break;
		case Natural_Disaster:
			classpks.addAll(setValue(96818, searchTerm));

			break;
		case Power_Energy:
			classpks.addAll(setValue(80490, searchTerm));

			break;
		case Sports:
			classpks.addAll(setValue(96820, searchTerm));

			break;
		case Statistics:
			classpks.addAll(setValue(96822, searchTerm));

			break;
		case Insurances:
			classpks.addAll(setValue(96824, searchTerm));
			break;
		case City_Development:
			classpks.addAll(setValue(96826, searchTerm));
			break;
		case Visas_Passports:
			classpks.addAll(setValue(8243647, searchTerm));
			break;
		default:
			new ArrayList<String>();
		}

		List<JournalArticle> journals = parseJournalArticleList(getJournalArticlesBySearchTerm(classpks, input, userId, searchTerm));
		int lastPageNo = journals.size() % 10 == 0 ? journals.size() / 10 : journals.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("services", byPaganation(journals, input));
		json.put("totalCount", 0);
		return json;
	}

	private JSONObject searchByLatest(String topic, String input, String userId) {
		JSONObject json = new JSONObject();
		List<Long> classpks = new ArrayList<Long>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85099);
			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
			List<JournalArticle> journalArticleList = parseJournalArticleList(getArticlesByUserId(classpks, input, userId));

			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			journalArticleList.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < journalArticleList.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("services", newArticles);
			json.put("totalCount", classpks.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForLiveStockService(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForServicesByLatest(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		List<JournalArticle> journalArticleList = parseJournalArticleList(getArticlesByUserId(classpks, input, userId));
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		journalArticleList.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < journalArticleList.size(); i++) {
			newArticles.add(stackList.pop());
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("services", newArticles);
		json.put("totalCount", classpks.size());
		return json;
	}

	private JSONObject searchByViewCount(String topic, String input, String userId) {
		JSONObject json = new JSONObject();
		List<Long> classpks = new ArrayList<Long>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPKListViewCount(85099);
			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;

			List<JournalArticle> journalArticleList = parseJournalArticleList(getArticlesByUserId(classpks, input, userId));

			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			journalArticleList.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < journalArticleList.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("services", newArticles);
			json.put("totalCount", classpks.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForLiveStockService(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForServicesByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;

		List<JournalArticle> journalArticleList = parseJournalArticleList(getArticlesByUserId(classpks, input, userId));

		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		journalArticleList.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < journalArticleList.size(); i++) {
			newArticles.add(stackList.pop());
		}

		json.put("lastPageNo", lastPageNo);
		json.put("services", newArticles);
		json.put("totalCount", classpks.size());
		json.put("lastInput", 0);
		return json;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServices(@RequestHeader("Authorization") String encryptedString, @RequestParam("topic") String topic, @RequestParam("input") String input, @RequestParam("viewby") String viewby, @RequestParam("userid") String userId) {
		JSONObject json = new JSONObject();

		if (!isValidPaganation(input)) {
			json.put("status", 0);
			json.put("message", "Page index out of range!");
			return json;
		}

		if (!isValidTopic(topic)) {
			json.put("status", 0);
			json.put("message", "Topic is not found!");
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

		ViewBy viewType = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewType) {
		case LATEST:
			return searchByLatest(topic, input, userId);
		case MOSTVIEW:
			return searchByViewCount(topic, input, userId);
		default:
			return new JSONObject();
		}
	}

	/* API to get rating form web Server */
	@RequestMapping(value = "getratingsentry", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public List<String> getRatingsEntryFromWeb(@RequestHeader(value = "classnameid") String classnameid, @RequestHeader(value = "classpk") String classpk) {
		List<RatingsEntry> ratingsEntriesFromWeb = ratingsEntryService.getScoresByClass(Long.parseLong(classnameid), Long.parseLong(classpk));

		List<String> entity = new ArrayList<String>();
		for (RatingsEntry entry : ratingsEntriesFromWeb) {
			StringBuilder str = new StringBuilder();
			str.append(entry.getUserid());
			str.append("," + entry.getScore());
			entity.add(str.toString());
		}
		return entity;
	}
}
