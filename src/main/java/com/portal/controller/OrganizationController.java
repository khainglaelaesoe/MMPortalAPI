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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgEngName;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.Organization_;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetCategoryService;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("organization")
public class OrganizationController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private AssetCategoryService assetCategoryService;

	@Value("${IMAGEURL}")
	private String IMAGEURL;

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

		String engPhNo = getEngElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"en_US\">");
		organization.setEngPhoneNo(engPhNo.replaceAll(" ", ""));

		String myaPhNo = getEngElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Phone", "<dynamic-content language-id=\"my_MM\">");
		organization.setMyanmarPhoneNo(myaPhNo.replaceAll(" ", ""));
		organization.setEngAddress(getEngElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"en_US\">"));
		organization.setMyanmarAddress(getEngElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "OfficeAddress", "<dynamic-content language-id=\"my_MM\">"));
		for (String engPh : organization.getEngPhoneNo().split(","))
			organization.getEngPhoneNoList().add(engPh.replaceAll(" ", ""));

		for (String myaPh : organization.getMyanmarPhoneNo().split("၊"))
			organization.getMyanmarPhoneNoList().add(myaPh.replaceAll(" ", ""));

		organization.setEngContent(journalArticle.getContent());
		return organization;
	}

	private List<Organization_> getOrganizationList(String articleInfo) {
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (String classpk : articleInfo.split(",")) {
			if (!classpk.isEmpty())
				organizationList.add(parseOrganization(journalArticleService.byClassPK(Long.parseLong(classpk.toString()))));
		}
		return organizationList;
	}

	@RequestMapping(value = "getContactUs", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getContactUs(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		List<Organization_> organizationList = new ArrayList<Organization_>();
		organizationList.add(parseContactUs(journalArticleService.getContactUsbyArticleAndVersion()));
		resultJson.put("contactInfo", organizationList);
		return resultJson;
	}

	@RequestMapping(value = "getEmergencyContacts", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getEmergencyContact(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
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
			ArrayList<Organization_> oldArr = new ArrayList<Organization_>();
			oldArr = new DocumentParsing().ParsingEmergencyContent(journal.getContent());
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
	public JSONObject getTerms(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		List<Organization_> organizationList = new ArrayList<Organization_>();
		//organizationList.add(parseTerms(journalArticleService.getTermsbyVersion()));
		//Organization_
		Organization_ organization = new Organization_();
		organization.setMmContent(terms);
		organization.setEngContent(terms);
		organizationList.add(organization);
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
		String mm = myanmarContent.replaceAll("website", "mobile application");
		String eng = engContent.replaceAll("website", "mobile application");
		organization.setMmContent(!mm.isEmpty() ? mm : eng);
		organization.setEngContent(!eng.isEmpty() ? eng : mm);
		return organization;
	}

	private List<Organization_> parseOrganizationList(List<JournalArticle> journals) {
		List<Organization_> organizationList = new ArrayList<Organization_>();
		journals.forEach(journal -> {
			organizationList.add(parseOrganization(journal));
		});
		return organizationList;
	}

	private List<Organization_> getOrganizationListBySearchTerm(String articleInfo, String searchTerm) {
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (String classpk : articleInfo.split(",")) {
			if (!classpk.isEmpty()) {
				JournalArticle journalArticle = journalArticleService.byClassPKAndSearchTerms(Long.parseLong(classpk.toString()), searchTerm);
				if (journalArticle != null)
					organizationList.add(parseOrganization(journalArticle));
			}
		}
		return organizationList;
	}

	@RequestMapping(value = "bysearchTerms", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getOrganizationBySearchTerms(@RequestHeader("Authorization") String encryptedString, @RequestParam("input") String searchTerm, @RequestParam("index") String index) {
		JSONObject resultJson = new JSONObject();

		if (!isValidPaganation(index)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Page index out of range!");
			return resultJson;
		}

		if (!isValidSearchTerm(searchTerm)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Avoid too many keyword!");
			return resultJson;
		}

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		List<Long> classpks = assetEntryService.getClasspkListBySearchTerm(91234, searchTerm);
		List<Organization_> orgs = getOrganizationList(convertToString(classpks, index));
		Stack<Organization_> stackList = new Stack<Organization_>();
		orgs.forEach(org -> {
			stackList.push(org);
		});

		List<Organization_> newArticles = new ArrayList<Organization_>();
		for (int i = 0; i < orgs.size(); i++) {
			newArticles.add(stackList.pop());
		}

		int lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("orginations", newArticles);
		resultJson.put("totalCount", 0);
		return resultJson;
	}

	@RequestMapping(value = "byname", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getOrganizationByName(@RequestHeader("Authorization") String encryptedString, @RequestParam("input") String input, @RequestParam("index") String index) throws UnsupportedEncodingException {
		JSONObject resultJson = new JSONObject();
		
		if (!isValidPaganation(index)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Page index out of range!");
			return resultJson;
		}

		if (!isValidOrgName(input)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Avoid too many keyword!");
			return resultJson;
		}
		
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		List<Long> classpks = new ArrayList<Long>();
		if (input.equals("all"))
			classpks = assetEntryService.getAssetEntryListByClassTypeIdAndOrderByPriority(91234);
		else {
			OrgMyanmarName orgName = OrgMyanmarName.valueOf(input);
			String value = orgName.getValue();
			switch (orgName) {
			case Ministry_of_Transport_and_Communications:
				value = "Ministry of Transport and Communications";
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Ministry_of_Construction:
				value = "Ministry of Construction";
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Ministry_of_Agriculture_Livestocks_and_Irrigation:
				value = "Ministry of Agriculture, Livestock and Irrigation";
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Napyitaw_City_Development_Committee:
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Yangon_City_Development_Committee:
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Ministry_of_Natural_Resources_and_Environmental_Conservation:
				value = "Ministry of Natural Resources and Environmental Conservation";
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Ministry_of_Labour_Immigration_and_Population:
				value = "Ministry of Labour, Immigration and Population";
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case Ministry_of_Social_Welfare_Relief_Resettlement:
				value = "Ministry of Social Welfare, Relief and Resettlement";
				classpks = assetEntryService.getAssetEntryListByKeyword(91234, value);
				break;
			case State_Regional_Hluttaws:
				List<String> names = assetCategoryService.getStateNames();
				for (String name : names)
					classpks.addAll(assetEntryService.getAssetEntryListByKeyword(91234, name));
				break;
			case Chin_State_Government:
				value = "ချင်းပြည်နယ်";
			default:
				classpks = assetEntryService.getAssetEntryListBySearchTerm(91234, value);
				break;
			}
		}

		int lastPageNo = classpks.size() % 10 == 0 ? classpks.size() / 10 : classpks.size() / 10 + 1;
		List<Organization_> orgs = getOrganizationList(convertToString(classpks, index));
		Stack<Organization_> stackList = new Stack<Organization_>();
		orgs.forEach(org -> {
			stackList.push(org);
		});

		List<Organization_> newArticles = new ArrayList<Organization_>();
		for (int i = 0; i < orgs.size(); i++) {
			newArticles.add(stackList.pop());
		}

		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", classpks.size());
		resultJson.put("orginations", newArticles);
		return resultJson;
	}

	@RequestMapping(value = "names", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getOrganizationName(@RequestHeader("Authorization") String encryptedString) {
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
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (OrgMyanmarName name : OrgMyanmarName.values()) {
			if (name != OrgMyanmarName.Myanmar_Computer_Federation && name != OrgMyanmarName.Topics && name != OrgMyanmarName.Ministry_of_Planning_and_Finance) {
				Organization_ org = new Organization_();
				org.setMyanmarName(name.getValue());
				org.setEngName(OrgEngName.valueOf(name.toString()).getValue());
				org.setKey(name.toString());
				if (!org.getMyanmarName().isEmpty())
					organizationList.add(org);
			}
		}
		json.put("organizationList", organizationList);
		return json;
	}

	@RequestMapping(value = "banner", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getBanner(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		String[] bannerList = new String[] { IMAGEURL + "banner01.jpg", IMAGEURL + "banner02.jpg"};
		resultJson.put("bannerList", bannerList);

		JSONObject[] array = new JSONObject[3];
		JSONObject object1 = new JSONObject();
		object1.put("banner", IMAGEURL + "banner01.jpg");
		JSONObject object2 = new JSONObject();
		object2.put("banner", IMAGEURL + "banner02.jpg");
		JSONObject object3 = new JSONObject();
		object3.put("banner", IMAGEURL + "banner04.jpg");
		array[0] = object1;
		array[1] = object2;
		array[2] = object3;
		resultJson.put("bannerObjects", array);
		return resultJson;
	}
	
	String terms = "<p><span>Thank you for visiting   Myanmar National Portal mobile application. By accessing and using this mobile application, you shall be deemed to have accepted to be legally bound by these Terms of Use. Please read them carefully. </span></p><p><b><span>General</span></b></p><p><span>1. These Terms of Use may be changed from time to time and your use of this mobile application after such changes have been posted will constitute your agreement to the modified Terms of Use and all of the changes.</span></p><p><b><span>Proprietary Rights</span></b></p><p><span>2. This mobile application is maintained by the Government of the Republic of the Union of Myanmar, Ministry of Transport and Communications.</span></p><p><span>"
			+ "3. The materials that assessed in this mobile application including the information and software programs are protected by copyright and other forms of proprietary rights. All rights, title and interest in the Contents are owned by, licensed to or controlled by Government on behalf of the Republic of the Union of Myanmar.</span></p><p><b><span>Restrictions on use of Materials</span></b></p><p><span>4. Materials featured on this  application may be reproduced FOC after taking proper permission by sending a mail to us. However, the material has to be reproduced accurately and not to be used in derogatory manner or in a misleading context. Wherever the material is being published or issued to others, the source must be prominently acknowledged. However, "
			+ "the permission to reproduce this material shall not extend to any material which is identified as being copyright of a third party. Authorizations to reproduce such material must be obtained from the departments/copyright holders concerned.</span></p><p><span>5. Modification of any of the Contents or use of the Contents for any other purpose will be a violation of copyright and other intellectual property rights. Graphics and images on this mobile application are protected by copyright and may not be reproduced or appropriated in any manner without written permission of government. </span></p><p><span>6. The Content on this application is for your personal use only and not for commercial exploitation.  "
			+ "Any unauthorized use of Myanmar National Mobile Application is prohibited.</span></p><p><b><span>Disclaimer of Warranties and Liability</span></b></p><p><span>7. The Contents of this mobile application are provided based on Myanmar National web portal. Though all efforts have been made to ensure the accuracy and currency of the content on this mobile application, the same should not be construed as a statement of law or used for any legal purposes. MOTC accepts no responsibility in relation to the accuracy, completeness, usefulness or otherwise, of the contents. Users are advised to verify/check any information with the relevant Government department(s) and/or other source(s), and to obtain any appropriate professional advice before acting on the information provided in the application."
			+ "</span></p><p><span>8. Government shall also not be liable for any damage or loss of any kind caused as a result (direct or indirect) of the use of the mobile application including but not limited to any damage or loss suffered as a result of reliance on the Contents contained in or available from the mobile application.</span></p><p><b><span>Right of Access</span></b></p><p><span>9. Government reserves all rights to deny or restrict access to this mobile application to any particular person, or to block access from a particular Internet address at any time, without ascribing any reasons whatsoever.</span></p><p><b><span>Links to other websites and Portal</span></b></p><p><span>10. This mobile application contains hyperlinks to websites which are not maintained by Government. Government is not responsible for the contents of those websites and shall not be liable for any damages or loss arising from access to those websites. "
			+ "Use of the hyperlinks and access to such websites are entirely at your own risk. </span></p><p><span>11. Hyperlinks to other websites are provided as a convenience. In no circumstances shall Government be considered to be associated or affiliated with any trade or service marks, logos, insignia or other devices used or appearing on websites to which this mobile application is linked. </span></p><p><b><span>Links to this mobile application from other websites</span></b></p><p><span>12. Caching and links to, and the framing of this mobile application or any of the Contents are prohibited. </span></p><p><span>13. You must secure permission from Government prior to hyperlinking to, or framing, this mobile application or any of the Contents, or engaging in similar activities. Government reserves the right to impose conditions when permitting any hyperlinking to, or framing of this mobile application or any of the Contents."
			+ " </span></p><p><span>14. You are linking to, or framing any part of this mobile application or its Contents constitute acceptance of these Terms of Use. This is deemed to be the case even after the posting of any changes or modifications to these Terms of Use. If you do not accept these Terms of Use, you must discontinue linking to, or framing of this application or any of the Contents.</span></p><p><span>15. In no circumstances shall be considered to be associated or affiliated in whatever manner with any trade or service marks, logos, insignia or other devices used or appearing on web the Contents. </span></p><p><span>16. Government reserves all rights to disable any links to, or frames of any site containing inappropriate, profane, defamatory, infringing, obscene, indecent or unlawful topics, names, material or information, or material or information that violates any written law, any applicable intellectual property, proprietary, privacy or publicity rights. "
			+ "</span></p><p><span>17. Government reserves the right to disable any unauthorized links or frames and disclaims any responsibility for the content available on any other site reached by links to or from this mobile application or any of the Contents. </span></p><p><b><span>Governing Law</span></b></p><p><span>18. These Terms of Use shall be governed and construed in accordance with laws of the Union of the Republic of Myanmar. Any dispute arising under these terms and conditions shall be subject to the exclusive jurisdiction of the courts of Myanmar. </span></p><p><span>If you’re not satisfied with our response to any privacy-related concern you may have, you can contact the Feedback. </span></p>";
}
