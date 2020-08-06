package com.portal.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
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

	public List<JournalArticle> getJournalArticles(List<String> entryList, String input) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		String info = convertEntryListToString(entryList, input);
		String[] classUuids = info.split(",");
		for (String classUuid : classUuids) {
			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(classUuid);
			if (journalArticle != null)
				journalArticleList.add(journalArticle);
		}
		return journalArticleList;
	}

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	private void setValue(long categoryId, String searchTerm, List<String> entryList, long totalCount) {
		List<Object> objectList = journalArticleService.getJobAndVacancyByTopicAndSearchTerm(categoryId, searchTerm);
		for (Object object : objectList) {
			Object[] obj = (Object[]) object;
			if (obj[0] == null || obj[1] == null)
				return;

			Long articleId = Long.parseLong(obj[0].toString());
			entryList.add(journalArticleService.getClassUuidByArticleId(articleId));
		}
	}

	@RequestMapping(value = "searchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getJobsBySearchTerm(@RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
		JSONObject json = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<String> entryList = new ArrayList<String>();

		long totalCount = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85090);
			List<JournalArticle> journalArticleList = getJournalArticles(entryList, input, searchTerm); // by size // now all

			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("jobs", byPaganation(parseJournalArticleList(journalArticleList), input));
			json.put("totalCount", journalArticleList.size());
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
		json.put("jobs", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

	private JSONObject getJobsByMostView(String topic, String input) {
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		JSONObject json = new JSONObject();

		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeIdAndViewCount(85090);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("jobs", parseJournalArticleList(getJournalArticles(entryList, input)));
			json.put("totalCount", entryList.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForLiveStockJobAndVacancy(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("jobs", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

	private JSONObject getJobsByLatest(String topic, String input) {
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		JSONObject json = new JSONObject();

		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85090);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("jobs", parseJournalArticleList(getJournalArticles(entryList, input)));
			json.put("totalCount", entryList.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForLiveStockJobAndVacancy(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForJobAndVacancyByLatest(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("jobs", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServices(@RequestParam("topic") String topic, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {

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
