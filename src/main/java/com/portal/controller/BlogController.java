package com.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.MBMessage;
import com.portal.entity.MobileResult;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.Reply;
import com.portal.entity.RequestVote;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;
import com.portal.service.MessageService;

@Controller
@RequestMapping("blog")
public class BlogController extends AbstractController {

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private JournalFolderService journalFolderService;

	@Autowired
	private MessageService messageService;

	private static Logger logger = Logger.getLogger(BlogController.class);

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newJournal.setEngTitle(title[0]);
		newJournal.setMynamrTitle(title[1]);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		newJournal.setEngDepartmentTitle(name);
		newJournal.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newJournal.setDisplaydate(resultDateString);

		String engContent = getEngElement(journalArticle.getContent(), "htmlContent", "<dynamic-content language-id=\"en_US\">");
		String myaContent = getEngElement(journalArticle.getContent(), "htmlContent", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "htmlContent", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "htmlContent", "<dynamic-content language-id=\"my_MM\">");

		newJournal.setEngContent(ImageSourceChange2(dp.ParsingSpan(engContent)));
		newJournal.setMyanmarContent(ImageSourceChange2(dp.ParsingSpan(myaContent)));

		newJournal.setShareLink(getShareLink(journalArticle.getUrltitle()));
		newJournal.setMessageList(journalArticle.getMessageList());
		newJournal.setpKString(journalArticle.getpKString());

		newJournal.setContent(journalArticle.getContent());
		return newJournal;
	}

	private String getShareLink(String urlTitle) {
		return "https://myanmar.gov.mm/blogs/-/asset_publisher/m9WiUYPkhQIm/content/" + urlTitle.replaceAll("%", "%25");
	}

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	@RequestMapping(value = "all", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getBlogs(@RequestParam("input") String input, @RequestParam("userid") String userId) {
		JSONObject resultJson = new JSONObject();
		// classTypeId=129731;
		List<Long> classPKList = assetEntryService.getClassPkList(129731);
		int lastPageNo = classPKList.size() % 10 == 0 ? classPKList.size() / 10 : classPKList.size() / 10 + 1;
		List<JournalArticle> journalArticleList = parseJournalArticleList(getAllArticles(classPKList, input, userId));
		List<JournalArticle> resultList = byPaganation(journalArticleList, input);
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		resultList.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < resultList.size(); i++) {
			newArticles.add(stackList.pop());
		}

		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("blog", newArticles);
		resultJson.put("totalCount", journalArticleList.size());
		return resultJson;
	}

	@RequestMapping(value = "bysearchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getBlogs(@RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("userid") String userId) {
		JSONObject resultJson = new JSONObject();
		List<Long> classPkList = assetEntryService.getClassPkList(129731);
		List<JournalArticle> journalArticleList = parseJournalArticleList(getArticles(classPkList, userId, searchterm));
		int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("blog", byPaganation(journalArticleList, input));
		resultJson.put("totalCount", 0);
		return resultJson;
	}

	@RequestMapping(value = "likecount", method = RequestMethod.GET)  // no need
	@ResponseBody
	@JsonView(Views.Thin.class)
	public String getlikecount(@RequestParam("messageid") String messageid) {
		String likecount = messageService.likeCount(Long.parseLong(messageid)) + "";
		return likecount;
	}

	@RequestMapping(value = "title", method = RequestMethod.GET) // no need
	@ResponseBody
	@JsonView(Views.Thin.class)
	public String getTitle(@RequestHeader("classpk") String classpk) {
		return journalArticleService.getTitleByClassPK(Long.parseLong(classpk));
	}
}
