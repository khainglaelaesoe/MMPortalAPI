package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
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
public class ServiceController {

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

		engContent = Jsoup.parse(content.substring(start, end)).text().replaceAll("value 1", "").replaceAll("<span style=\"color:#000080;\">", "<span>").replaceAll("<span style=\"color:#0000ff;\">", "<span>");
		String remainString = content.substring(end, content.length());

		int mStart = remainString.indexOf("<dynamic-content language-id=\"my_MM\">");
		if (mStart > 0) {
			int mEnd = remainString.lastIndexOf("</dynamic-content>");
			myanmarContent = Jsoup.parse(remainString.substring(mStart, mEnd)).text().replaceAll("value 1", "").replaceAll("<span style=\"color:#000080;\">", "<span>").replaceAll("<span style=\"color:#0000ff;\">", "<span>");
		}

		newJournal.setMyanmarContent(!myanmarContent.isEmpty() ? myanmarContent : engContent);
		newJournal.setEngContent(!engContent.isEmpty() ? engContent : myanmarContent);

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newJournal.setDisplaydate(resultDateString);
		newJournal.setId_(journalArticle.getId_());
		newJournal.setRating(journalArticle.getRating());
		newJournal.setRatingAction(journalArticle.getRatingAction());
		newJournal.setShareLink(journalArticle.getShareLink());
		newJournal.setClassNameString(journalArticle.getClassNameString());
		newJournal.setpKString(journalArticle.getpKString());
		newJournal.setUserRating(journalArticle.getUserRating());
		return newJournal;
	}

	private String convertObjectListToString(List<String> entryList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (entryList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++)
			info += entryList.get(i) + ",";
		return info;
	}

	public List<JournalArticle> getJournalArticles(List<String> entryList, String input, String userId) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		String info = convertObjectListToString(entryList, input);
		String[] classUuids = info.split(",");
		for (String classUuid : classUuids) {

			Long entryId = assetEntryService.getClassNameByClassUuid(classUuid).get(0);
			Long classNameId = assetEntryService.getClassName(classUuid).get(0);
			Long classPK = assetEntryService.getClassPK(classUuid).get(0);

			List<RatingsEntry> ratingsEntriesFromWeb = ratingsEntryService.getScoresByClass(classNameId, classPK);
			List<String> mobileRatingsEntries = getRatingsEntry(classNameId.toString(), classPK.toString());

			double totalScore = 0;
			String ratingAction = "no";
			double userRating = 0.0;
			for (RatingsEntry entry : ratingsEntriesFromWeb) {
				totalScore += entry.getScore();

				/* web user id */
				String webUserId = getWebUserId(userId);
				logger.info("webUserId!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + webUserId);
				if (Long.parseLong(webUserId) == entry.getUserid()) {
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

			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(classUuid);
			if (journalArticle != null) {
				journalArticle.setRating(CollectionUtils.isEmpty(ratingsEntriesFromWeb) && CollectionUtils.isEmpty(mobileRatingsEntries) ? 0 : totalScore / (ratingsEntriesFromWeb.size() + mobileRatingsEntries.size()));
				journalArticle.setUserRating(userRating);
				journalArticle.setShareLink(getShareLink(journalArticle.getUrltitle(), entryId.toString()));
				journalArticle.setClassNameString(classNameId.toString());
				journalArticle.setpKString(classPK.toString());
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

	private void setValue(long categoryId, String searchTerm, List<String> entryList, long totalCount) {
		List<Object> objectList = journalArticleService.getServiceByTopicAndSearchTerm(categoryId, searchTerm);
		for (Object object : objectList) {
			Object[] obj = (Object[]) object;
			if (obj[0] == null)
				return;

			Long articleId = Long.parseLong(obj[0].toString());
			entryList.add(journalArticleService.getClassUuidByArticleId(articleId));
		}
	}

	private List<JournalArticle> getResultList(List<String> entryList, String input, String searchTerm, String userId) {
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<JournalArticle> journalArticleList = getJournalArticles(entryList, input, userId);
		journalArticleList.forEach(journalArticle -> {
			StringBuilder searchTerms = new StringBuilder();
			searchTerms.append(journalArticle.getContent());
			searchTerms.append(journalArticle.getTitle());
			if (searchTerms.toString().contains(searchTerm))
				resultList.add(journalArticle);
		});
		return resultList;
	}

	@RequestMapping(value = "searchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getServicesBySearchTerm(@RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic, @RequestParam("userid") String userId) {
		JSONObject json = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<String> entryList = new ArrayList<String>();

		long totalCount = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85099);
			int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

			while (resultList.size() < 10 && Integer.parseInt(input) < lastPageNo) {
				resultList.addAll(getResultList(entryList, input, searchTerm, userId));
				input = (Integer.parseInt(input) + 1) + "";
			}

			json.put("lastPageNo", lastPageNo);
			json.put("services", parseJournalArticleList(resultList));
			json.put("totalCount", journalArticleService.getAllBySearchterm(searchTerm, 85099));
			json.put("lastInput", input);
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			setValue(80486, searchTerm, entryList, totalCount);
			break;
		case Education_Research:
			setValue(80484, searchTerm, entryList, totalCount);
			break;
		case Social:
			setValue(80485, searchTerm, entryList, totalCount);
			break;
		case Economy:
			setValue(96793, searchTerm, entryList, totalCount);
			break;
		case Agriculture:
			setValue(80491, searchTerm, entryList, totalCount);
			break;
		case Labour_Employment:
			setValue(80494, searchTerm, entryList, totalCount);
			break;
		case Livestock:
			setValue(87834, searchTerm, entryList, totalCount);
			break;
		case Law_Justice:
			setValue(96797, searchTerm, entryList, totalCount);
			break;
		case Security:
			setValue(96799, searchTerm, entryList, totalCount);
			break;
		case Hotel_Tourism:
			setValue(80488, searchTerm, entryList, totalCount);
			break;
		case Citizen:
			setValue(96801, searchTerm, entryList, totalCount);
			break;
		case Natural_Resources_Environment:
			setValue(80501, searchTerm, entryList, totalCount);
			break;
		case Industries:
			setValue(80495, searchTerm, entryList, totalCount);
			break;
		case Construction:
			setValue(96804, searchTerm, entryList, totalCount);
			break;
		case Science:
			setValue(80499, searchTerm, entryList, totalCount);
			break;
		case Technology:
			setValue(80496, searchTerm, entryList, totalCount);
			break;
		case Transportation:
			setValue(97769, searchTerm, entryList, totalCount);
			break;
		case Communication:
			setValue(96809, searchTerm, entryList, totalCount);
			break;
		case Information_Media:
			setValue(96815, searchTerm, entryList, totalCount);
			break;
		case Religion_Art_Culture:
			setValue(80493, searchTerm, entryList, totalCount);
			break;
		case Finance_Tax:
			setValue(80489, searchTerm, entryList, totalCount);
			break;
		case SMEs:
			setValue(80503, searchTerm, entryList, totalCount);
			break;
		case Natural_Disaster:
			setValue(96818, searchTerm, entryList, totalCount);
			break;
		case Power_Energy:
			setValue(80490, searchTerm, entryList, totalCount);
			break;
		case Sports:
			setValue(96820, searchTerm, entryList, totalCount);
			break;
		case Statistics:
			setValue(96822, searchTerm, entryList, totalCount);
			break;
		case Insurances:
			setValue(96824, searchTerm, entryList, totalCount);
			break;
		case City_Development:
			setValue(96826, searchTerm, entryList, totalCount);
			break;
		case Visas_Passports:
			setValue(8243647, searchTerm, entryList, totalCount);
			break;
		default:
			new ArrayList<String>();
		}

		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("services", CollectionUtils.isEmpty(entryList) ? new ArrayList<JournalArticle>() : parseJournalArticleList(getJournalArticles(entryList, input, userId)));
		json.put("totalCount", entryList.size());
		return json;
	}

	private JSONObject searchByLatest(String topic, String input, String userId) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85099);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList, input, userId));
			json.put("services", journalArticleList);
			json.put("totalCount", entryList.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForLiveStockService(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("services", parseJournalArticleList(getJournalArticles(entryList, input, userId)));
		json.put("totalCount", entryList.size());
		return json;
	}

	private JSONObject searchByViewCount(String topic, String input, String userId) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeIdAndViewCount(85099);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList, input, userId));
			json.put("lastPageNo", lastPageNo);
			json.put("services", journalArticleList);
			json.put("totalCount", entryList.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForLiveStockService(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForServicesByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("services", parseJournalArticleList(getJournalArticles(entryList, input, userId)));
		json.put("totalCount", entryList.size());
		return json;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServices(@RequestParam("topic") String topic, @RequestParam("input") String input, @RequestParam("viewby") String viewby, @RequestParam("userid") String userId) {
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

	public List<String> getRatingsEntry(String classNameId, String classPk) {

		// Prepare the header
		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("classnameid", classNameId);
		headers.add("classpk", classPk);

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		// Prepare the URL
		String url = SERVICEURL + "/user/getratingsentry";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		// RESTTemplate to call the service
		RestTemplate restTemplate = new RestTemplate();

		// Data type for response
		HttpEntity<List> response = null;
		try {

			restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, List.class);

			List<String> userScores = response.getBody();
			logger.info("LeaveBalance list size:" + userScores.size());

			return userScores;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new ArrayList<String>();
	}

	public String getWebUserId(String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("userid", userId);

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/user/webuserid";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = null;
		try {

			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, String.class);
			logger.info("response.getBody()!!!!!!!!!!!!!!:" + response.getBody());
			return response.getBody();

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return null;
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
