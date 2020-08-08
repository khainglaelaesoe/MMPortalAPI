package com.portal.controller;

import java.io.UnsupportedEncodingException;
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
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgEngName;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.Organization_;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("organization")
public class OrganizationController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	private String convertToString(List<Long> articles, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (articles.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++) {
			if (articles.get(i) != null) {
				Long o1 = articles.get(i);
				if (o1 != null)
					info += o1.toString() + ",";
			}
		}
		return info;
	}

	public void replaceTag(Elements els) {
		ListIterator<Element> iter = els.listIterator();
		while (iter.hasNext()) {
			Element el = iter.next();
			replaceTag(el.children());
			if (el.parentNode() != null)
				el.replaceWith(new TextNode("/" + el.text().trim() + "/"));
		}
	}

	private Organization_ parseOrganization(JournalArticle journalArticle) {

		Organization_ organization = new Organization_();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		organization.setEngName(title[0]);
		organization.setMyanmarName(title[1]);

		organization.setEmail(getEngElement(journalArticle.getContent(), "Email", "<dynamic-content language-id=\"en_US\">").isEmpty() ? getEngElement(journalArticle.getContent(), "Email", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Email", "<dynamic-content language-id=\"en_US\">"));
		organization.setEngPhoneNo(getEngElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"en_US\">"));
		organization.setMyanmarPhoneNo(getEngElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"my_MM\">"));
		organization.setEngAddress(getEngElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"en_US\">"));
		organization.setMyanmarAddress(getEngElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"my_MM\">"));
		for (String engPh : organization.getEngPhoneNo().split(","))
			organization.getEngPhoneNoList().add(engPh);

		for (String myaPh : organization.getMyanmarPhoneNo().split("၊"))
			organization.getMyanmarPhoneNoList().add(myaPh);

		organization.setEngContent(journalArticle.getContent());
		return organization;
	}

	private List<Organization_> getOrganizationList(String articleInfo) {
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (String id : articleInfo.split(",")) {
			if (!DateUtil.isEmpty(id)) {
				organizationList.add(parseOrganization(journalArticleService.getJournalArticle(Long.parseLong(id))));
			}
		}
		return organizationList;
	}

	private List<Organization_> getOrganizationListBySearchTerm(String articleInfo, String searchTerm) {
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (String id : articleInfo.split(",")) {
			if (!DateUtil.isEmpty(id)) {
				JournalArticle journalArticle = journalArticleService.getJournalArticle(Long.parseLong(id));
				if (journalArticle != null)
					organizationList.add(parseOrganization(journalArticle));
			}
		}
		return organizationList;
	}

	@RequestMapping(value = "getContactUs", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getContactUs() {
		JSONObject resultJson = new JSONObject();
		List<Organization_> organizationList = new ArrayList<Organization_>();
		organizationList.add(parseContactUs(journalArticleService.getContactUsbyArticleAndVersion()));
		resultJson.put("contactInfo", organizationList);
		return resultJson;
	}

//	@RequestMapping(value = "getEmergencyContacts", method = RequestMethod.GET)
//	@ResponseBody
//	@JsonView(Views.Summary.class)
//	public JSONObject getEmergencyContact() {
//		JSONObject resultJson = new JSONObject();
//		List<Organization> organizationList = new ArrayList<Organization>();
//		organizationList = parseEmergencyContact(journalArticleService.getEmergenyContact());
//		resultJson.put("emergencyContent", organizationList);
//		return resultJson;
//	}
	@RequestMapping(value = "getEmergencyContacts", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getEmergencyContact() {
		JSONObject resultJson = new JSONObject();
		List<Organization_> organizationList = new ArrayList<Organization_>();
		organizationList = parseEmergencyContact(journalArticleService.getEmergenyContact());
		resultJson.put("emergencyContent", organizationList);
		return resultJson;
	}

	private List<Organization_> parseEmergencyContact(List<JournalArticle> journalArticle) {
		List<Organization_> orgList = new ArrayList<Organization_>();
		for (JournalArticle journal : journalArticle) {
			Organization_ organization = new Organization_();
			String title[] = new DocumentParsing().ParsingTitle(journal.getTitle());
			organization.setEngName(title[0]);
			organization.setMyanmarName(title[1]);

			Document doc = Jsoup.parse(journal.getContent());
			replaceTag(doc.children());
			String[] contentInfo = Jsoup.parse(doc.toString()).text().split("/");
			List<String> infoList = new ArrayList<String>();
			for (int i = 0; i < contentInfo.length; i++) {
				if (contentInfo[i] != null && !contentInfo[i].isEmpty() && contentInfo[i].length() > 1)
					infoList.add(contentInfo[i]);
			}
			ArrayList<Organization_> oldArr = new ArrayList<Organization_>();
			Organization_ org = new Organization_();
			int j = 0;
			for (String i : infoList) {
				if (i.contains("0") || i.contains("191"))
					org.setEngPhoneNo(i);
				else if (i.trim().contains("၀") || i.trim().contains("၁၉၁"))
					org.setMyanmarPhoneNo(i);
				else if ((i.trim().charAt(0) >= 'a' && i.trim().charAt(0) <= 'z') || (i.trim().charAt(0) >= 'A' && i.trim().charAt(0) <= 'Z'))
					org.setEngContent(i);
				else
					org.setMmContent(i);
				j++;
				if (j > 3) {
					oldArr.add(org);
					org = new Organization_();
					j = 0;
				}
			}
			organization.setSeeMore(oldArr);
			orgList.add(organization);
		}
		return orgList;
	}

	private Organization_ parseContactUs(JournalArticle journalArticle) {
		Document titledoc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(titledoc.children());
		String[] titleInfo = Jsoup.parse(titledoc.toString()).text().split("/");

		Organization_ organization = new Organization_();
		organization.setEngName(titleInfo[6] != null && !titleInfo[6].isEmpty() ? titleInfo[6] : titleInfo[8]);
		organization.setMyanmarName(titleInfo.length >= 8 && titleInfo[8] != null && !titleInfo[8].isEmpty() ? titleInfo[8] : titleInfo[6]);
		String[] engmyan = new DocumentParsing().ParsingContent(journalArticle.getContent());
		String engContent = engmyan[0];
		String myanContent = engmyan[1];
		Document engdoc = Jsoup.parse(engContent, "", Parser.htmlParser());
		Elements engpara = engdoc.getElementsByTag("p");
		Document myandoc = Jsoup.parse(myanContent, "", Parser.htmlParser());
		Elements myanpara = myandoc.getElementsByTag("p");
		String engAddress = "", engPhoneNo = "", email = "";
		String myanAddress = "", myanmarPhoneNo = "", myanEmail = "";
		for (Element e : engpara) {
			if (e.text().toString().contains("phone")) {
				engPhoneNo = e.text().toString();
			}
			if (e.text().toString().contains("email")) {
				email = e.text().toString();
			}
			if (!e.text().toString().contains("email") && !e.text().toString().contains("phone")) {
				String ele = e.text().toString();
				if (!ele.equals(""))
					if (!engAddress.equals(""))
						engAddress = engAddress + "\n" + e.text().toString();
					else
						engAddress = e.text().toString();
			}
		}
		for (Element e : myanpara) {
			if (e.text().toString().contains("၀")) {
				myanmarPhoneNo = e.text().toString();
			}
			if (e.text().toString().contains("@")) {
				myanEmail = e.text().toString();
			}
			if (!e.text().toString().contains("@") && !e.text().toString().contains("၀")) {
				String ele = e.text().toString();
				if (!ele.equals(""))
					if (!myanAddress.equals(""))
						myanAddress = myanAddress + "\n" + e.text().toString();
					else
						myanAddress = e.text().toString();
			}
		}

		organization.setEngPhoneNo(engPhoneNo);
		organization.setEmail(email);
		organization.setMyanmarPhoneNo(myanmarPhoneNo);
		organization.setMyanEmail(myanEmail);
		organization.setEngAddress(engAddress);
		organization.setMyanmarAddress(myanAddress);
		return organization;
	}

	@RequestMapping(value = "getTerms", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getTerms() {
		JSONObject resultJson = new JSONObject();
		List<Organization_> organizationList = new ArrayList<Organization_>();
		organizationList.add(parseTerms(journalArticleService.getTermsbyVersion()));
		resultJson.put("termsInfo", organizationList);
		return resultJson;
	}

	private Organization_ parseTerms(JournalArticle journalArticle) {
		Document titledoc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(titledoc.children());
		String[] titleInfo = Jsoup.parse(titledoc.toString()).text().split("/");

		Organization_ organization = new Organization_();
		organization.setEngName(titleInfo[6] != null && !titleInfo[6].isEmpty() ? titleInfo[6] : titleInfo[8]);
		organization.setMyanmarName(titleInfo.length >= 8 && titleInfo[8] != null && !titleInfo[8].isEmpty() ? titleInfo[8] : titleInfo[6]);
		// Content
		String engContent = "", myanmarContent = "";
		String content = journalArticle.getContent();
		int begin = content.indexOf("\"text_area\"");
		content = content.substring(begin, content.length());

		int start = content.indexOf("<dynamic-content language-id=\"en_US\">");
		int end = content.indexOf("</dynamic-content>");

		engContent = Jsoup.parse(content.substring(start, end)).text().replaceAll("value 1", "");
		String remainString = content.substring(end, content.length());

		int mStart = remainString.indexOf("<dynamic-content language-id=\"my_MM\">");
		if (mStart > 0) {
			int mEnd = remainString.lastIndexOf("</dynamic-content>");
			myanmarContent = Jsoup.parse(remainString.substring(mStart, mEnd)).text().replaceAll("value 1", "");
		}

		organization.setMmContent(!myanmarContent.isEmpty() ? myanmarContent : engContent);
		organization.setEngContent(!engContent.isEmpty() ? engContent : myanmarContent);
		return organization;
	}

	@RequestMapping(value = "byname", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getOrganizationByName(@RequestParam("input") String input, @RequestParam("index") String index) throws UnsupportedEncodingException {
		JSONObject resultJson = new JSONObject();
		List<Long> articles = new ArrayList<Long>();
		if (input.equals("all")) {
			List<String> entryList = assetEntryService.getAssetEntryListByClassTypeIdAndOrderByPriority(91234);
			for (String classuuid : entryList)
				articles.addAll(journalArticleService.getIdByFolderId(classuuid)); // desc -> reverse
		} else {
			String value = OrgEngName.valueOf(input).getValue();
			List<String> entryList = assetEntryService.getAssetEntryListByName(91234, value);
			for (String classuuid : entryList)
				articles.addAll(journalArticleService.getIdByFolderId(classuuid)); // desc -> reverse
		}

		int lastPageNo = articles.size() % 10 == 0 ? articles.size() / 10 : articles.size() / 10 + 1;

		List<Organization_> orgs = getOrganizationList(convertToString(articles, index));
		Stack<Organization_> stackList = new Stack<Organization_>();
		orgs.forEach(org -> {
			stackList.push(org);
		});

		List<Organization_> newArticles = new ArrayList<Organization_>();
		for (int i = 0; i < orgs.size(); i++) {
			newArticles.add(stackList.pop());
		}

		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", articles.size());
		resultJson.put("orginations", newArticles);
		return resultJson;
	}

	@RequestMapping(value = "names", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getOrganizationName() {
		JSONObject json = new JSONObject();
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (OrgMyanmarName name : OrgMyanmarName.values()) {
			Organization_ org = new Organization_();
			org.setMyanmarName(name.getValue());
			org.setEngName(OrgEngName.valueOf(name.toString()).getValue());
			org.setKey(name.toString());
			organizationList.add(org);
		}
		json.put("organizationList", organizationList);
		return json;
	}

	@RequestMapping(value = "bysearchTerms", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getOrganizationBySearchTerms(@RequestParam("input") String input, @RequestParam("index") String index) {
		JSONObject resultJson = new JSONObject();
		List<Long> articles = journalArticleService.getIdByFolderIdAndSearchTerms(91278, input);
		int lastPageNo = articles.size() % 10 == 0 ? articles.size() / 10 : articles.size() / 10 + 1;
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("orginations", getOrganizationListBySearchTerm(convertToString(articles, index), input));
		return resultJson;
	}
}
