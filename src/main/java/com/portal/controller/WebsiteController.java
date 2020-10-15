package com.portal.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
import com.portal.entity.JournalArticle;
import com.portal.entity.Organization_;
import com.portal.entity.TableData;
import com.portal.entity.Views;
import com.portal.entity.govdata;
import com.portal.parsing.DocumentParsing;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("website")
public class WebsiteController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	@RequestMapping(value = "getGovwebsite", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getGovernmentWebsite(@RequestHeader("Authorization") String encryptedString) {
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

		Organization_ org = new Organization_();
		JournalArticle jarticle = new JournalArticle();
		jarticle = journalArticleService.getJournalArticleforGov();
		org = parseGovsite(jarticle, "");
		resultJson.put("govsite", org);
		return resultJson;
	}

	@RequestMapping(value = "getGovwebsiteforother", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public Organization_ getGovernmentWebsiteforOther(@RequestHeader("Authorization") String encryptedString) {

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (isAuthorize(decryptedString)) {
				Organization_ org = new Organization_();
				JournalArticle jarticle = new JournalArticle();
				jarticle = journalArticleService.getJournalArticleforGov();
				org = parseGovsite(jarticle, "iso");
				return org;
			}
		} catch (Exception e) {
			logger.error("Error: " + e);
		}

		return null;
	}

	private Organization_ parseGovsite(JournalArticle journalArticle, String type) {

		Organization_ organization = new Organization_();

		// Content
		String[] engmyan = new DocumentParsing().ParsingTable(journalArticle.getContent());
		String engContent = engmyan[0];
		String myanContent = engmyan[1];
		ArrayList<TableData> engdata, myandata;
		if (type.equals("ios")) {
			engdata = govWebsiteformatforIOS(engContent);
			myandata = govWebsiteformatforIOS(myanContent);
		} else {
			engdata = govWebsiteformat(engContent);
			myandata = govWebsiteformat(myanContent);
		}

		organization.setTableEngData(engdata);
		organization.setTableMyanData(myandata);
		return organization;
	}

	public ArrayList<TableData> govWebsiteformat(String str1) {
		ArrayList<TableData> tblDataArr = new ArrayList<TableData>();

		Document doc1 = Jsoup.parse(str1.toString(), "", Parser.xmlParser());
		Elements element1 = doc1.getElementsByTag("tr");
		ArrayList<String> deptNoArr = new ArrayList<String>();// deptNo
		ArrayList<String> deptArr = new ArrayList<String>();// dept
		ArrayList<String> websiteArr = new ArrayList<String>();// website
		element1.remove(0);// to remove th
		deptNoArr.add("");
		int j = 0;
		TableData tblData = new TableData();
		for (Element e : element1) {
			j++;
			Document doc2 = Jsoup.parse(e.toString(), "", Parser.xmlParser());
			Elements element2 = doc2.getElementsByTag("td");
			String mno = Jsoup.parse(element2.get(0).toString()).text();// MNo
			String ministryName = Jsoup.parse(element2.get(1).toString()).text();// MName
			String deptNo = Jsoup.parse(element2.get(2).toString()).text();// dept no
			String dept = Jsoup.parse(element2.get(3).toString()).text();// dept
			String website = Jsoup.parse(element2.get(4).toString()).text();// website
			System.out.println("J___________" + j + "Element1________________" + element1.size() + "Ministry No__________" + mno);
			if (mno.equals("") && ministryName.equals("") && deptNo.equals("") && dept.equals("") && website.equals("")) {
				tblData.setDeptNo(deptNoArr);
				tblData.setDepartment(deptArr);
				tblData.setWebsite(websiteArr);
				tblDataArr.add(tblData);
				tblData = new TableData();
				deptNoArr = new ArrayList<String>();
				deptArr = new ArrayList<String>();
				websiteArr = new ArrayList<String>();
			} else if (mno.equals("") && ministryName.equals("")) {
				deptNoArr.add(deptNo.trim());// deptNo
				deptArr.add(dept.trim());// dept
				websiteArr.add(website.trim());// website
			} else {
				tblData.setMiniStryNo(mno);
				tblData.setMinistry(ministryName);
				deptNoArr.add(deptNo.trim());// deptNo
				deptArr.add(dept.trim());// dept
				websiteArr.add(website.trim());// website
				if (j == element1.size()) {
					tblData.setDeptNo(deptNoArr);
					tblData.setDepartment(deptArr);
					tblData.setWebsite(websiteArr);
					tblDataArr.add(tblData);
					tblData = new TableData();
					deptNoArr = new ArrayList<String>();
					deptArr = new ArrayList<String>();
					websiteArr = new ArrayList<String>();
				}
			}

		} // for

		return tblDataArr;
	}

	public ArrayList<TableData> govWebsiteformatforIOS(String str1) {
		ArrayList<TableData> tblDataArr = new ArrayList<TableData>();
		Document doc1 = Jsoup.parse(str1.toString(), "", Parser.xmlParser());
		Elements element1 = doc1.getElementsByTag("tr");
		element1.remove(0);// to remove th
		int j = 0;
		TableData tblData = new TableData();
		ArrayList<govdata> dataArr = new ArrayList<govdata>();
		for (Element e : element1) {
			j++;
			govdata govdata = new govdata();
			Document doc2 = Jsoup.parse(e.toString(), "", Parser.xmlParser());
			Elements element2 = doc2.getElementsByTag("td");
			String mno = Jsoup.parse(element2.get(0).toString()).text();// MNo
			String ministryName = Jsoup.parse(element2.get(1).toString()).text();// MName
			govdata.setDeptNo(Jsoup.parse(element2.get(2).toString()).text());
			;// dept no
			govdata.setDept(Jsoup.parse(element2.get(3).toString()).text());
			;// dept
			govdata.setWebsite(Jsoup.parse(element2.get(4).toString()).text());
			;// website
			if (mno.equals("") && ministryName.equals("") && govdata.getDeptNo().equals("") && govdata.getDept().equals("") && govdata.getWebsite().equals("")) {
				tblData.setDataArrList(dataArr);
				tblDataArr.add(tblData);
				tblData = new TableData();
				govdata = new govdata();
				dataArr = new ArrayList<govdata>();
			} else if (mno.equals("") && ministryName.equals("")) {
				dataArr.add(govdata);// deptNo
			} else {
				tblData.setMiniStryNo(mno);
				tblData.setMinistry(ministryName);
				dataArr.add(govdata);// deptNo
				if (j == element1.size()) {
					tblData.setDataArrList(dataArr);
					tblDataArr.add(tblData);
				}
			}

		} // for

		return tblDataArr;
	}
}
