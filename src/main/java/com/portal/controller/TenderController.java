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
		List<String> conentList = removeDelimeterFrom(content);
		newJournal.setDisplaydate(!CollectionUtils.isEmpty(conentList) ? conentList.get(conentList.size() - 1) : "");

		/* image url */
		int start = content.indexOf("http") < 0 ? content.indexOf("/document") : content.indexOf("http");
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("]]");
		newJournal.setImageUrl(remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end));
		return newJournal;
	}

	public List<JournalArticle> getJournalArticles(List<Long> classpks, String input) {

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

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	public List<JournalArticle> setValue(long categoryId, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		List<Object> objectList = journalArticleService.getTenderByTopicAndSearchTerm(categoryId, searchTerm);
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
	public JSONObject getTendersBySearchTerm(@RequestHeader("Authorization") String encryptedString, @RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
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
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();

		long totalCount = 0;
		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85086);
			List<JournalArticle> journalArticles = getJournalArticlesBySearchTerm(classpks, searchTerm); // by size // now all

			int lastPageNo = journalArticles.size() % 10 == 0 ? journalArticles.size() / 10 : journalArticles.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("tenders", byPaganation(parseJournalArticleList(journalArticles), input));
			json.put("totalCount", 0);
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			journalArticleList.addAll(setValue(80486, searchTerm));
			break;
		case Education_Research:
			journalArticleList.addAll(setValue(80484, searchTerm));
			break;
		case Social:
			journalArticleList.addAll(setValue(80485, searchTerm));
			break;
		case Economy:
			journalArticleList.addAll(setValue(96793, searchTerm));
			break;
		case Agriculture:
			journalArticleList.addAll(setValue(80491, searchTerm));
			break;
		case Labour_Employment:
			journalArticleList.addAll(setValue(80494, searchTerm));
			break;
		case Livestock:
			journalArticleList.addAll(setValue(87834, searchTerm));
			break;
		case Law_Justice:
			journalArticleList.addAll(setValue(96797, searchTerm));
			break;
		case Security:
			journalArticleList.addAll(setValue(96799, searchTerm));
			break;
		case Hotel_Tourism:
			journalArticleList.addAll(setValue(80488, searchTerm));
			break;
		case Citizen:
			journalArticleList.addAll(setValue(96801, searchTerm));
			break;
		case Natural_Resources_Environment:
			journalArticleList.addAll(setValue(80501, searchTerm));
			break;
		case Industries:
			journalArticleList.addAll(setValue(80495, searchTerm));
			break;
		case Construction:
			journalArticleList.addAll(setValue(96804, searchTerm));

			break;
		case Science:
			journalArticleList.addAll(setValue(80499, searchTerm));

			break;
		case Technology:
			journalArticleList.addAll(setValue(80496, searchTerm));

			break;
		case Transportation:
			journalArticleList.addAll(setValue(97769, searchTerm));

			break;
		case Communication:
			journalArticleList.addAll(setValue(96809, searchTerm));

			break;
		case Information_Media:
			journalArticleList.addAll(setValue(96815, searchTerm));

			break;
		case Religion_Art_Culture:
			journalArticleList.addAll(setValue(80493, searchTerm));

			break;
		case Finance_Tax:
			journalArticleList.addAll(setValue(80489, searchTerm));

			break;
		case SMEs:
			journalArticleList.addAll(setValue(80503, searchTerm));

			break;
		case Natural_Disaster:
			journalArticleList.addAll(setValue(96818, searchTerm));

			break;
		case Power_Energy:
			journalArticleList.addAll(setValue(80490, searchTerm));

			break;
		case Sports:
			journalArticleList.addAll(setValue(96820, searchTerm));

			break;
		case Statistics:
			journalArticleList.addAll(setValue(96822, searchTerm));

			break;
		case Insurances:
			journalArticleList.addAll(setValue(96824, searchTerm));

			break;
		case City_Development:
			journalArticleList.addAll(setValue(96826, searchTerm));

			break;
		case Visas_Passports:
			journalArticleList.addAll(setValue(8243647, searchTerm));
			break;
		default:
			new ArrayList<String>();
		}

		int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("tenders", byPaganation(parseJournalArticleList(journalArticleList), input));
		json.put("totalCount", 0);
		return json;
	}

	private JSONObject getTendersByLatest(String topic, String input) {
		JSONObject json = new JSONObject();
		List<Long> classpks = new ArrayList<Long>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85086);
			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;

			List<JournalArticle> tenders = getJournalArticles(classpks, input);
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
			json.put("totalCount", classpks.size());
			return json;
		}
		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForTendersByLatest(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		List<JournalArticle> tenders = getJournalArticles(classpks, input);

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
		json.put("totalCount", classpks.size());
		return json;

	}

	private JSONObject getTendersByMostView(String topic, String input) {
		JSONObject json = new JSONObject();
		List<Long> classpks = new ArrayList<Long>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85086);

			List<JournalArticle> tenders = getJournalArticles(classpks, input);
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			tenders.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < tenders.size(); i++) {
				newArticles.add(stackList.pop());
			}

			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("tenders", newArticles);
			json.put("totalCount", classpks.size());
			return json;
		}

		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForTendersByViewCount(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("tenders", getJournalArticles(classpks, input));
		json.put("totalCount", classpks.size());
		return json;

	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getTenders(@RequestHeader("Authorization") String encryptedString, @RequestParam("topic") String topic, @RequestParam("input") String input, @RequestParam("viewby") String viewby) {
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
			return getTendersByLatest(topic, input);
		case MOSTVIEW:
			return getTendersByMostView(topic, input);
		default:
			return new JSONObject();
		}
	}
}
