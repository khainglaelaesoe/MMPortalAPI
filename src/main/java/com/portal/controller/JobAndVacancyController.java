package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.TopicEngName;
import com.portal.entity.ViewBy;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;

@Controller
@RequestMapping("jobandvacancy")
public class JobAndVacancyController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalFolderService journalFolderService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	private String getClosingDate(String content) {
		int start = content.indexOf("closingDate");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int startIndex = remainString.indexOf("CDATA[") + 6;
		int endIndex = remainString.indexOf("]]");
		return remainString.substring(startIndex, endIndex);
	}

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* title, agency, date, image url */

		/* myanamr title, english title */
		JournalArticle newJournal = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newJournal.setEngTitle(title[0]);
		newJournal.setMynamrTitle(title[1]);

		/* department myanamr title, department english title */
		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		newJournal.setEngDepartmentTitle(name);
		newJournal.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_").replaceAll("'", "")).getValue());

		/* date */
		String content = journalArticle.getContent();
		newJournal.setDisplaydate(getClosingDate(content));

		/* image url */
		String imageUrl = "";
		int start = content.indexOf("http") < 0 ? content.indexOf("/document") : content.indexOf("http");
		if (start < 0)
			imageUrl = "";
		else {
			String remainString = content.substring(start, content.length());
			int end = remainString.indexOf("]]");
			imageUrl = remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
		}
		newJournal.setImageUrl(imageUrl);
		newJournal.setContent(content);
		return newJournal;
	}

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	public List<JournalArticle> setValue(long categoryId, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		List<Object> objectList = journalArticleService.getJobAndVacancyByTopicAndSearchTerm(categoryId, searchTerm);
		if (CollectionUtils.isEmpty(objectList))
			return journalArticleList;

		for (Object object : objectList) {
			Object[] obj = (Object[]) object;
			if (obj[0] == null)
				continue;

			Long articleId = Long.parseLong(obj[0].toString());
			String version = obj[1].toString();

			if (articleId == null || version == null)
				continue;

			journalArticleList.add(journalArticleService.getJournalArticleByArticleIdAndVersion(articleId, version));
		}
		return journalArticleList;
	}

	@RequestMapping(value = "searchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getJobsBySearchTerm(@RequestHeader("Authorization") String encryptedString, @RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
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
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();

		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85090);
			List<JournalArticle> journalArticleList = getJournalArticlesBySearchTerm(classpks, searchTerm);

			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("jobs", byPaganation(parseJournalArticleList(journalArticleList), input));
			json.put("totalCount", 0);
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			journalArticles.addAll(setValue(80486, searchTerm));
			break;
		case Education_Research:
			journalArticles.addAll(setValue(80484, searchTerm));
			break;
		case Social:
			journalArticles.addAll(setValue(80485, searchTerm));
			break;
		case Economy:
			journalArticles.addAll(setValue(96793, searchTerm));
			break;
		case Agriculture:
			journalArticles.addAll(setValue(80491, searchTerm));
			break;
		case Labour_Employment:
			journalArticles.addAll(setValue(80494, searchTerm));
			break;
		case Livestock:
			journalArticles.addAll(setValue(87834, searchTerm));
			break;
		case Law_Justice:
			journalArticles.addAll(setValue(96797, searchTerm));
			break;
		case Security:
			journalArticles.addAll(setValue(96799, searchTerm));
			break;
		case Hotel_Tourism:
			journalArticles.addAll(setValue(80488, searchTerm));
			break;
		case Citizen:
			journalArticles.addAll(setValue(96801, searchTerm));
			break;
		case Natural_Resources_Environment:
			journalArticles.addAll(setValue(80501, searchTerm));
			break;
		case Industries:
			journalArticles.addAll(setValue(80495, searchTerm));
			break;
		case Construction:
			journalArticles.addAll(setValue(96804, searchTerm));

			break;
		case Science:
			journalArticles.addAll(setValue(80499, searchTerm));

			break;
		case Technology:
			journalArticles.addAll(setValue(80496, searchTerm));

			break;
		case Transportation:
			journalArticles.addAll(setValue(97769, searchTerm));

			break;
		case Communication:
			journalArticles.addAll(setValue(96809, searchTerm));

			break;
		case Information_Media:
			journalArticles.addAll(setValue(96815, searchTerm));

			break;
		case Religion_Art_Culture:
			journalArticles.addAll(setValue(80493, searchTerm));

			break;
		case Finance_Tax:
			journalArticles.addAll(setValue(80489, searchTerm));

			break;
		case SMEs:
			journalArticles.addAll(setValue(80503, searchTerm));

			break;
		case Natural_Disaster:
			journalArticles.addAll(setValue(96818, searchTerm));

			break;
		case Power_Energy:
			journalArticles.addAll(setValue(80490, searchTerm));

			break;
		case Sports:
			journalArticles.addAll(setValue(96820, searchTerm));

			break;
		case Statistics:
			journalArticles.addAll(setValue(96822, searchTerm));

			break;
		case Insurances:
			journalArticles.addAll(setValue(96824, searchTerm));

			break;
		case City_Development:
			journalArticles.addAll(setValue(96826, searchTerm));

			break;
		case Visas_Passports:
			journalArticles.addAll(setValue(8243647, searchTerm));
			break;
		default:
			new ArrayList<String>();
		}

		int lastPageNo = journalArticles.size() % 10 == 0 ? journalArticles.size() / 10 : journalArticles.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("jobs", byPaganation(parseJournalArticleList(journalArticles), input));
		json.put("totalCount", 0);
		return json;
	}

	private JSONObject getJobsByMostView(String topic, String input) {
		List<Long> classpks = new ArrayList<Long>();
		int lastPageNo = 0;
		JSONObject json = new JSONObject();

		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPKListViewCount(85090);
			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;

			List<JournalArticle> jobs = getArticles(classpks, input);
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			jobs.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < jobs.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("jobs", jobs);
			json.put("totalCount", classpks.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForLiveStockJobAndVacancy(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		List<JournalArticle> jobs = getArticles(classpks, input);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		jobs.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < jobs.size(); i++) {
			newArticles.add(stackList.pop());
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("jobs", newArticles);
		json.put("totalCount", classpks.size());
		return json;
	}

	public List<JournalArticle> getArticles(List<Long> classpks, String input) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		if (CollectionUtils.isEmpty(classpks))
			return journalArticleList;

		String info = convertLongListToString(classpks, input);
		String[] classpkList = info.split(",");
		for (String classpk : classpkList) {
			JournalArticle journalArticle = journalArticleService.byClassPK(Long.parseLong(classpk));
			if (journalArticle != null)
				journalArticleList.add(parseJournalArticle(journalArticle));
		}

		return journalArticleList;
	}

	private JSONObject getJobsByLatest(String topic, String input) {
		List<Long> classpks = new ArrayList<Long>();
		int lastPageNo = 0;
		JSONObject json = new JSONObject();

		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85090);
			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;

			List<JournalArticle> jobs = getArticles(classpks, input);
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			jobs.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < jobs.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("jobs", newArticles);
			json.put("totalCount", classpks.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForLiveStockJobAndVacancy(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		List<JournalArticle> jobs = getArticles(classpks, input);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		jobs.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < jobs.size(); i++) {
			newArticles.add(stackList.pop());
		}

		json.put("lastPageNo", lastPageNo);
		json.put("jobs", newArticles);
		json.put("totalCount", classpks.size());
		return json;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServices(@RequestHeader("Authorization") String encryptedString, @RequestParam("topic") String topic, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {
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
		ViewBy viewBy = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewBy) {
		case LATEST:
			return getJobsByLatest(topic, input);
		case MOSTVIEW:
			return getJobsByMostView(topic, input);
		default:
			return new JSONObject();
		}
	}
}
