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
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

import com.portal.entity.TableData;
import com.portal.entity.TopicEngName;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.serive.impl.JournalArticleServiceImpl;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("image")
public class ImageController extends AbstractController {

	@Autowired
	private JournalArticleServiceImpl jornalService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);
	
	@RequestMapping(value = "getImages", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getOrganizationByName() throws UnsupportedEncodingException {
		JSONObject resultJson = new JSONObject();
		List<Organization_> orgList = new ArrayList<Organization_>();
		orgList =parseImages( jornalService.getImagebyClassuuid());
		resultJson.put("images",orgList);
		return resultJson;
	}
	private List<Organization_> parseImages(List<JournalArticle> journalList) {
		List<Organization_> orgList = new ArrayList<Organization_>();
		for(JournalArticle journalArticle : journalList) {
		Organization_ organization = new Organization_();
		String[] engmyanTitle = new DocumentParsing().ParsingTitle(journalArticle.getTitle());
		String engTitle = engmyanTitle[0];
		String myanTitle = engmyanTitle[1];
		String[] engmyan = new DocumentParsing().ParsingContentImage(journalArticle.getContent());
		String engImage = engmyan[0];
		String myanImage = engmyan[1];
		organization.setEngContentTitle(engTitle != null && !engTitle.isEmpty()? engTitle : myanTitle);
		organization.setMyanmarContentTitle(myanTitle != null && !myanTitle.isEmpty()? myanTitle: engTitle);
		organization.setEngContent(engImage != null && !engImage.isEmpty()? engImage : myanImage);
		organization.setMmContent(myanImage != null && !myanImage.isEmpty()? myanImage: engImage);
		orgList.add(organization);
		}
		return orgList;
	}
}
