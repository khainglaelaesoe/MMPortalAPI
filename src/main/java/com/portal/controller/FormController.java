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

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}
	
	public List<JournalArticle> setValue(long categoryId, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		List<Object> objectList = journalArticleService.getFormByTopicAndSearchTerm(categoryId, searchTerm);
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
	public JSONObject getDocumentsBySearchTerm(@RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
		JSONObject json = new JSONObject();
		List<Long> classpks = new ArrayList<Long>();
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();

		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(85212);
			List<JournalArticle> journalArticleList = getJournalArticlesBySearchTerm(classpks, searchTerm); // by size // now all
			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("forms", byPaganation(parseJournalArticleList(journalArticleList), input));
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
		json.put("forms", byPaganation(parseJournalArticleList(journalArticles), input));
		json.put("totalCount", 0);
		return json;
	}

	private List<JournalArticle> getJournalArticles(String classPkInfo) {
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		for (String classpk : classPkInfo.split(",")) {
			if (!classpk.isEmpty()) {
				JournalArticle journalArticle = journalArticleService.byClassPK(Long.parseLong(classpk));
				if (journalArticle != null)
					journalArticles.add(parseJournalArticle(journalArticle));
			}
		}
		return journalArticles;
	}

	@RequestMapping(value = "topic", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServices(@RequestParam("topic") String topic, @RequestParam("input") String input) {
		JSONObject json = new JSONObject();
		List<Long> classPKs = new ArrayList<Long>();
		int lastPageNo = 0;
		if (topic.equals("all")) {
			classPKs = assetEntryService.getClassPkList(85212);
			lastPageNo = classPKs.size() % 10 == 0 ? classPKs.size() / 10 : classPKs.size() / 10 + 1;

			List<JournalArticle> forms = getJournalArticles(convertLongListToString(classPKs, input)); 
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			forms.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < forms.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("forms", newArticles);
			json.put("totalCount", classPKs.size());
			return json;
		}
		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classPKs = assetEntryService.getClassPKsForForm(80486);
			break;
		case Education_Research:
			classPKs = assetEntryService.getClassPKsForForm(80484);
			break;
		case Social:
			classPKs = assetEntryService.getClassPKsForForm(80485);
			break;
		case Economy:
			classPKs = assetEntryService.getClassPKsForForm(96793);
			break;
		case Agriculture:
			classPKs = assetEntryService.getClassPKsForForm(80491);
			break;
		case Labour_Employment:
			classPKs = assetEntryService.getClassPKsForForm(80494);
			break;
		case Livestock:
			classPKs = assetEntryService.getClassPKsForForm(87834);
			break;
		case Law_Justice:
			classPKs = assetEntryService.getClassPKsForForm(96797);
			break;
		case Security:
			classPKs = assetEntryService.getClassPKsForForm(96799);
			break;
		case Hotel_Tourism:
			classPKs = assetEntryService.getClassPKsForForm(80488);
			break;
		case Citizen:
			classPKs = assetEntryService.getClassPKsForForm(96801);
			break;
		case Natural_Resources_Environment:
			classPKs = assetEntryService.getClassPKsForForm(80501);
			break;
		case Industries:
			classPKs = assetEntryService.getClassPKsForForm(80495);
			break;
		case Construction:
			classPKs = assetEntryService.getClassPKsForForm(96804);
			break;
		case Science:
			classPKs = assetEntryService.getClassPKsForForm(80499);
			break;
		case Technology:
			classPKs = assetEntryService.getClassPKsForForm(80496);
			break;
		case Transportation:
			classPKs = assetEntryService.getClassPKsForForm(97769);
			break;
		case Communication:
			classPKs = assetEntryService.getClassPKsForForm(96809);
			break;
		case Information_Media:
			classPKs = assetEntryService.getClassPKsForForm(96815);
			break;
		case Religion_Art_Culture:
			classPKs = assetEntryService.getClassPKsForForm(80493);
			break;
		case Finance_Tax:
			classPKs = assetEntryService.getClassPKsForForm(80489);
			break;
		case SMEs:
			classPKs = assetEntryService.getClassPKsForForm(80503);
			break;
		case Natural_Disaster:
			classPKs = assetEntryService.getClassPKsForForm(96818);
			break;
		case Power_Energy:
			classPKs = assetEntryService.getClassPKsForForm(80490);
			break;
		case Sports:
			classPKs = assetEntryService.getClassPKsForForm(96820);
			break;
		case Statistics:
			classPKs = assetEntryService.getClassPKsForForm(96822);
			break;
		case Insurances:
			classPKs = assetEntryService.getClassPKsForForm(96824);
			break;
		case City_Development:
			classPKs = assetEntryService.getClassPKsForForm(96826);
			break;
		case Visas_Passports:
			classPKs = assetEntryService.getClassPKsForForm(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = classPKs.size() % 10 == 0 ? classPKs.size() / 10 : classPKs.size() / 10 + 1;

		List<JournalArticle> forms = getJournalArticles(convertLongListToString(classPKs, input));

		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		forms.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newForms = new ArrayList<JournalArticle>();
		for (int i = 0; i < forms.size(); i++) {
			newForms.add(stackList.pop());
		}
		json.put("lastPageNo", lastPageNo);
		json.put("forms", newForms);
		json.put("totalCount", classPKs.size());
		return json;
	}

}
