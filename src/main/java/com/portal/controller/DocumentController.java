package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
import com.portal.entity.TopicEngName;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("document")
public class DocumentController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

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

	private List<String> removeDelimeter(String str) {
		Document doc = Jsoup.parse(str);
		replaceTag(doc.children());
		String[] contentInfo = Jsoup.parse(doc.toString()).text().split("/");
		return removeInvalidString(contentInfo);
	}

	private String getAttribute(String content, String delimeter) {
		int start = content.indexOf(delimeter);
		String remainString = content.substring(start, content.length());
		int startIndex = remainString.indexOf("CDATA[") + 6;
		int endIndex = remainString.indexOf("]]");
		return remainString.substring(startIndex, endIndex).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(startIndex, endIndex) : remainString.substring(startIndex, endIndex);
	}

	private String getLink(String content) {
		int start = content.indexOf("uploadDocument") < 0 ? content.indexOf("externalURL") : content.indexOf("uploadDocument");
		String remainString = content.substring(start, content.length());
		int startIndex = remainString.indexOf("CDATA[") + 6;
		int endIndex = remainString.indexOf("]]");
		String result = remainString.substring(startIndex, endIndex);

		if (result.isEmpty()) {
			String remainString2 = remainString.substring(endIndex, remainString.length());
			int startIndex2 = remainString2.indexOf("CDATA[") + 6;
			int endIndex2 = remainString2.lastIndexOf("]]");
			result = remainString2.substring(startIndex2, endIndex2);
		}

		return result.startsWith("/") ? "https://myanmar.gov.mm" + result : result;
	}

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* myanmar title, english title */
		JournalArticle newJournal = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newJournal.setEngTitle(title[0]);
		newJournal.setMynamrTitle(title[1]);

		String content = journalArticle.getContent();
		newJournal.setPublicationDate(getAttribute(content, "PublicationDate"));
		newJournal.setPage(getAttribute(content, "Pages"));

		newJournal.setEngPblisher(getEngElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"en_US\">"));
		newJournal.setMyanmarPublisher(getMyanmarElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"my_MM\">"));
		
		/* image url and download link */
		newJournal.setEngImageUrl(getAttribute(content, "SmallImage").contains("http") ? getAttribute(content, "SmallImage") : getImage(content));
		newJournal.setEngDownloadLink(getLink(content).isEmpty() ? getAttribute(content, "externalURL") : getLink(content));
		newJournal.setContent(journalArticle.getContent());
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
		List<Object> objectList = journalArticleService.getDocumentByTopicAndSearchTerm(categoryId, searchTerm);
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
	public JSONObject getDocumentsBySearchTerm(@RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
		JSONObject json = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<String> entryList = new ArrayList<String>();

		long totalCount = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(84948);
			totalCount = journalArticleService.getJobBySearchterm(searchTerm, 84948);
			List<JournalArticle> journalArticleList =  getJournalArticles(entryList, input, searchTerm); // by size // now all
			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("documents",  byPaganation(parseJournalArticleList(journalArticleList), input));
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
		json.put("documents", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getDocuments(@RequestParam("topic") String topic, @RequestParam("input") String input) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(84948);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("documents", parseJournalArticleList(getJournalArticles(entryList, input)));
			json.put("totalCount", entryList.size());
			return json;
		}
		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForDocument(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForDocument(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForDocument(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForDocument(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForDocument(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForDocument(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForDocument(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForDocument(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForDocument(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForDocument(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForDocument(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForDocument(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForDocument(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForDocument(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForDocument(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForDocument(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForDocument(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForDocument(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForDocument(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForDocument(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForDocument(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForDocument(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForDocument(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForDocument(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForDocument(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForDocument(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForDocument(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForDocument(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForDocument(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("documents", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

}
