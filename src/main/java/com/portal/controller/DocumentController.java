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

		/* Page */
		newJournal.setEngPage(getEngElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"en_US\">"));
		newJournal.setMyaPage(getEngElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Pages", "<dynamic-content language-id=\"my_MM\">"));

		/* Language */
		newJournal.setEngLanguage(getEngElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"en_US\">"));
		newJournal.setMyaLanguage(getEngElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Language", "<dynamic-content language-id=\"my_MM\">"));

		newJournal.setEngPblisher(getEngElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"en_US\">"));
		newJournal.setMyanmarPublisher(getMyanmarElement(journalArticle.getContent(), "Publisher", "<dynamic-content language-id=\"my_MM\">"));

		/* image url and download link */
		newJournal.setEngImageUrl(getAttribute(content, "SmallImage").contains("http") ? getAttribute(content, "SmallImage") : getImage(content));
		newJournal.setEngDownloadLink(getLink(content).isEmpty() ? getAttribute(content, "externalURL") : getLink(content));
		newJournal.setContent(journalArticle.getContent());
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
		List<Object> objectList = journalArticleService.getDocumentByTopicAndSearchTerm(categoryId, searchTerm);
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
	public JSONObject getDocumentsBySearchTerm(@RequestHeader("Authorization") String encryptedString,@RequestParam("searchterm") String searchTerm, @RequestParam("input") String input, @RequestParam("topic") String topic) {
		JSONObject json = new JSONObject();
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
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		List<Long> classpks = new ArrayList<Long>();

		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(84948);
			List<JournalArticle> journalArticleList = getJournalArticlesBySearchTerm(classpks, searchTerm); // by size // now all
			int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
			json.put("lastPageNo", lastPageNo);
			json.put("documents", byPaganation(parseJournalArticleList(journalArticleList), input));
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
		json.put("documents", byPaganation(parseJournalArticleList(journalArticles), input));
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
	@JsonView(Views.Summary.class)
	public JSONObject getDocuments(@RequestHeader("Authorization") String encryptedString,@RequestParam("topic") String topic, @RequestParam("input") String input) {
		JSONObject json = new JSONObject();
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
		int lastPageNo = 0;
		if (topic.equals("all")) {
			classpks = assetEntryService.getClassPkList(84948);
			lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;

			List<JournalArticle> documents = getJournalArticles(convertLongListToString(classpks, input));
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			documents.forEach(article -> {
				stackList.push(article);
			});

			List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
			for (int i = 0; i < documents.size(); i++) {
				newArticles.add(stackList.pop());
			}

			json.put("lastPageNo", lastPageNo);
			json.put("documents", newArticles);
			json.put("totalCount", classpks.size());
			return json;
		}
		TopicEngName topicName = TopicEngName.valueOf(topic);
		switch (topicName) {
		case Health:
			classpks = assetEntryService.getAssetEntryListForDocument(80486);
			break;
		case Education_Research:
			classpks = assetEntryService.getAssetEntryListForDocument(80484);
			break;
		case Social:
			classpks = assetEntryService.getAssetEntryListForDocument(80485);
			break;
		case Economy:
			classpks = assetEntryService.getAssetEntryListForDocument(96793);
			break;
		case Agriculture:
			classpks = assetEntryService.getAssetEntryListForDocument(80491);
			break;
		case Labour_Employment:
			classpks = assetEntryService.getAssetEntryListForDocument(80494);
			break;
		case Livestock:
			classpks = assetEntryService.getAssetEntryListForDocument(87834);
			break;
		case Law_Justice:
			classpks = assetEntryService.getAssetEntryListForDocument(96797);
			break;
		case Security:
			classpks = assetEntryService.getAssetEntryListForDocument(96799);
			break;
		case Hotel_Tourism:
			classpks = assetEntryService.getAssetEntryListForDocument(80488);
			break;
		case Citizen:
			classpks = assetEntryService.getAssetEntryListForDocument(96801);
			break;
		case Natural_Resources_Environment:
			classpks = assetEntryService.getAssetEntryListForDocument(80501);
			break;
		case Industries:
			classpks = assetEntryService.getAssetEntryListForDocument(80495);
			break;
		case Construction:
			classpks = assetEntryService.getAssetEntryListForDocument(96804);
			break;
		case Science:
			classpks = assetEntryService.getAssetEntryListForDocument(80499);
			break;
		case Technology:
			classpks = assetEntryService.getAssetEntryListForDocument(80496);
			break;
		case Transportation:
			classpks = assetEntryService.getAssetEntryListForDocument(97769);
			break;
		case Communication:
			classpks = assetEntryService.getAssetEntryListForDocument(96809);
			break;
		case Information_Media:
			classpks = assetEntryService.getAssetEntryListForDocument(96815);
			break;
		case Religion_Art_Culture:
			classpks = assetEntryService.getAssetEntryListForDocument(80493);
			break;
		case Finance_Tax:
			classpks = assetEntryService.getAssetEntryListForDocument(80489);
			break;
		case SMEs:
			classpks = assetEntryService.getAssetEntryListForDocument(80503);
			break;
		case Natural_Disaster:
			classpks = assetEntryService.getAssetEntryListForDocument(96818);
			break;
		case Power_Energy:
			classpks = assetEntryService.getAssetEntryListForDocument(80490);
			break;
		case Sports:
			classpks = assetEntryService.getAssetEntryListForDocument(96820);
			break;
		case Statistics:
			classpks = assetEntryService.getAssetEntryListForDocument(96822);
			break;
		case Insurances:
			classpks = assetEntryService.getAssetEntryListForDocument(96824);
			break;
		case City_Development:
			classpks = assetEntryService.getAssetEntryListForDocument(96826);
			break;
		case Visas_Passports:
			classpks = assetEntryService.getAssetEntryListForDocument(8243647);
			break;
		default:
			new ArrayList<String>();
		}

		lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		List<JournalArticle> documents = getJournalArticles(convertLongListToString(classpks, input));
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		documents.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < documents.size(); i++) {
			newArticles.add(stackList.pop());
		}

		json.put("lastPageNo", lastPageNo);
		json.put("documents", newArticles);
		json.put("totalCount", classpks.size());
		return json;
	}

}
