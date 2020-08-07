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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
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
@RequestMapping("tender")
public class TenderController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalFolderService journalFolderService;

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

	private List<String> removeDelimeterFromContent(String content) {
		Document contentDoc = Jsoup.parse(content);
		replaceTag(contentDoc.children());
		String[] contentInfo = Jsoup.parse(contentDoc.toString()).text().split("/");
		return removeInvalidString(contentInfo);
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
		List<String> conentList = removeDelimeterFromContent(content);
		newJournal.setDisplaydate(!CollectionUtils.isEmpty(conentList) ? conentList.get(conentList.size() - 1) : "");

		/* image url */
		int start = content.indexOf("http") < 0 ? content.indexOf("/document") : content.indexOf("http");
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("]]");
		newJournal.setImageUrl(remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end));
		return newJournal;
	}

	public List<JournalArticle> getJournalArticles(List<String> entryList, String input) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		String info = convertEntryListToString(entryList, input);
		String[] classUuids = info.split(",");
		logger.info("classUuids: !!!!!!!!!!!!!!!!!!!!!!!!!" + classUuids.length);
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
		List<Object> objectList = journalArticleService.getTenderByTopicAndSearchTerm(categoryId, searchTerm);
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
	public JSONObject getTendersBySearchTerm(@RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();

		long totalCount = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85086);
			List<JournalArticle> journalArticleList = getJournalArticles(entryList, input, searchTerm); // by size // now all

			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("tenders", byPaganation(parseJournalArticleList(journalArticleList), input));
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
		json.put("tenders", CollectionUtils.isEmpty(entryList) ? new ArrayList<JournalArticle>() : parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

	private JSONObject getTendersByLatest(String topic, String input) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85086);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

			List<JournalArticle> tenders = parseJournalArticleList(getJournalArticles(entryList, input));
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			tenders.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < tenders.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("tenders", newArticles);
			json.put("totalCount", entryList.size());
			return json;
		}
		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForTendersByLatest(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		List<JournalArticle> tenders = parseJournalArticleList(getJournalArticles(entryList, input));
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		tenders.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < tenders.size(); i++) {
			newArticles.add(stackList.pop());
		}
		json.put("lastPageNo", lastPageNo);
		json.put("tenders", newArticles);
		json.put("totalCount", entryList.size());
		return json;

	}

	private JSONObject getTendersByMostView(String topic, String input) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeIdAndViewCount(85086);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("tenders", parseJournalArticleList(getJournalArticles(entryList, input)));
			json.put("totalCount", entryList.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForTendersByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("tenders", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;

	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getTenders(@RequestParam("topic") String topic, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {

		ViewBy viewBy = ViewBy.valueOf(viewby.toUpperCase().trim());
		switch (viewBy) {
		case LATEST:
			return getTendersByLatest(topic, input);
		case MOSTVIEW:
			return getTendersByMostView(topic, input);
		default:
			return new JSONObject();
		}
	}
}
