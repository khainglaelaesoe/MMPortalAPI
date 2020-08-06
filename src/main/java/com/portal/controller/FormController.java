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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgEngName;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.TopicEngName;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;

@Controller
@RequestMapping("form")
public class FormController extends AbstractController {

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

	private String getLink(String content, String remover) {
		int index = content.indexOf("Form") < 0 ? content.indexOf("uploadForm") : content.indexOf("Form");
		String remainString = content.substring(index, content.length());

		int start = remainString.indexOf(remover);
		if (start < 0)
			return "";

		String remainString2 = remainString.substring(start, remainString.length());
		int startIndex = remainString2.indexOf("CDATA[") + 6;
		int endIndex = remainString2.indexOf("]]");
		String result = remainString2.substring(startIndex, endIndex);

		if (result.isEmpty()) {
			String remainString3 = remainString2.substring(endIndex, remainString2.length());
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

	private String getOnlineForm(String content, String remover) {
		int index = content.indexOf("onlineForm");
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

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* title, department title, download link , image url */

		JournalArticle newJournal = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newJournal.setEngTitle(title[0]);
		newJournal.setMynamrTitle(title[1]);

		/* department myanamr title, department english title */
		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		if (name.equals("Topics")) {
			newJournal.setEngDepartmentTitle("");
			newJournal.setMyanmarDepartmentTitle("");
		} else {
			newJournal.setEngDepartmentTitle(OrgEngName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_").replaceAll("'", "")).getValue());
			newJournal.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_").replaceAll("'", "")).getValue());

		}
		/* image url */
		String content = journalArticle.getContent();
		int end = 0;
		String remainString;
		int start = content.indexOf("/image/");
		if (start > 0) {
			remainString = content.substring(start, content.length());
			end = remainString.indexOf("<");
		} else {
			start = content.indexOf("/document");
			remainString = content.substring(start, content.length());
			end = remainString.indexOf("]]");
		}
		newJournal.setImageUrl("https://myanmar.gov.mm" + remainString.substring(0, end));
		newJournal.setEngDownloadLink(getLink(content, "en_US"));
		newJournal.setMyanamrDownloadLink(getLink(content, "my_MM"));
		newJournal.setMyanmarOnlineForm(getOnlineForm(content, "my_MM"));
		newJournal.setEngOnlineForm(getOnlineForm(content, "en_US"));
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
		List<Object> objectList = journalArticleService.getFormByTopicAndSearchTerm(categoryId, searchTerm);
		for (Object object : objectList) {
			Object[] obj = (Object[]) object;
			if (obj[0] == null)
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
		List<String> entryList = new ArrayList<String>();

		long totalCount = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85212);
			List<JournalArticle> journalArticleList = getJournalArticles(entryList, input, searchTerm); // by size // now all
			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("forms", byPaganation(parseJournalArticleList(journalArticleList), input));
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
		json.put("forms", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServices(@RequestParam("topic") String topic, @RequestParam("input") String input) {
		JSONObject json = new JSONObject();
		List<String> entryList = new ArrayList<String>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			entryList = assetEntryService.getAssetEntryListByClassTypeId(85212);
			lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("forms", parseJournalArticleList(getJournalArticles(entryList, input)));
			json.put("totalCount", entryList.size());
			return json;
		}
		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			entryList = assetEntryService.getAssetEntryListForForm(80486);
			break;
		case Education_Research:
			entryList = assetEntryService.getAssetEntryListForForm(80484);
			break;
		case Social:
			entryList = assetEntryService.getAssetEntryListForForm(80485);
			break;
		case Economy:
			entryList = assetEntryService.getAssetEntryListForForm(96793);
			break;
		case Agriculture:
			entryList = assetEntryService.getAssetEntryListForForm(80491);
			break;
		case Labour_Employment:
			entryList = assetEntryService.getAssetEntryListForForm(80494);
			break;
		case Livestock:
			entryList = assetEntryService.getAssetEntryListForForm(87834);
			break;
		case Law_Justice:
			entryList = assetEntryService.getAssetEntryListForForm(96797);
			break;
		case Security:
			entryList = assetEntryService.getAssetEntryListForForm(96799);
			break;
		case Hotel_Tourism:
			entryList = assetEntryService.getAssetEntryListForForm(80488);
			break;
		case Citizen:
			entryList = assetEntryService.getAssetEntryListForForm(96801);
			break;
		case Natural_Resources_Environment:
			entryList = assetEntryService.getAssetEntryListForForm(80501);
			break;
		case Industries:
			entryList = assetEntryService.getAssetEntryListForForm(80495);
			break;
		case Construction:
			entryList = assetEntryService.getAssetEntryListForForm(96804);
			break;
		case Science:
			entryList = assetEntryService.getAssetEntryListForForm(80499);
			break;
		case Technology:
			entryList = assetEntryService.getAssetEntryListForForm(80496);
			break;
		case Transportation:
			entryList = assetEntryService.getAssetEntryListForForm(97769);
			break;
		case Communication:
			entryList = assetEntryService.getAssetEntryListForForm(96809);
			break;
		case Information_Media:
			entryList = assetEntryService.getAssetEntryListForForm(96815);
			break;
		case Religion_Art_Culture:
			entryList = assetEntryService.getAssetEntryListForForm(80493);
			break;
		case Finance_Tax:
			entryList = assetEntryService.getAssetEntryListForForm(80489);
			break;
		case SMEs:
			entryList = assetEntryService.getAssetEntryListForForm(80503);
			break;
		case Natural_Disaster:
			entryList = assetEntryService.getAssetEntryListForForm(96818);
			break;
		case Power_Energy:
			entryList = assetEntryService.getAssetEntryListForForm(80490);
			break;
		case Sports:
			entryList = assetEntryService.getAssetEntryListForForm(96820);
			break;
		case Statistics:
			entryList = assetEntryService.getAssetEntryListForForm(96822);
			break;
		case Insurances:
			entryList = assetEntryService.getAssetEntryListForForm(96824);
			break;
		case City_Development:
			entryList = assetEntryService.getAssetEntryListForForm(96826);
			break;
		case Visas_Passports:
			entryList = assetEntryService.getAssetEntryListForForm(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		json.put("lastPageNo", lastPageNo);
		json.put("forms", parseJournalArticleList(getJournalArticles(entryList, input)));
		json.put("totalCount", entryList.size());
		return json;
	}

}
