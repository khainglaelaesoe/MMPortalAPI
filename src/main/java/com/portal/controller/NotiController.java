package com.portal.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.CategoryType;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;

@Controller
@RequestMapping("noti")
public class NotiController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private JournalFolderService journalFolderService;

	@Autowired
	private AssetEntryService assetEntryService;

	private static Logger logger = Logger.getLogger(NotiController.class);

	private String getClosingDate(String content) {
		int start = content.indexOf("closingDate");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int startIndex = remainString.indexOf("CDATA[") + 6;
		int endIndex = remainString.indexOf("]]");
		return remainString.substring(startIndex, endIndex);
	}

	private JournalArticle parseJobAndVacancy(JournalArticle journalArticle) {
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
		newJournal.setDisplaydate(getClosingDate(content));

		/* image url */
		String imageUrl = "";
		int start = content.indexOf("http") < 0 ? content.indexOf("/document") : content.indexOf("http");
		if (start < 0)
			imageUrl = "";
		else {
			String remainString = content.substring(start, content.length());
			int end = remainString.indexOf("]]");
			imageUrl = remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
		}
		newJournal.setImageUrl(imageUrl);

		newJournal.setContent(content);
		return newJournal;
	}

	private JournalArticle parseTender(JournalArticle journalArticle) {
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

		newJournal.setContent(newJournal.getDisplaydate());
		/* image url */
		int start = content.indexOf("http") < 0 ? content.indexOf("/document") : content.indexOf("http");
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("]]");
		newJournal.setImageUrl(remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end));
		return newJournal;
	}

	private List<JournalArticle> getJournalObjects(Long calssTypeId) {
		Date date = new Date();
		String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(date);

		List<JournalArticle> entities = new ArrayList<JournalArticle>();
		List<Long> classpks = journalArticleService.getAssetEntryListByClassTypeIdAndOrderByPriority(calssTypeId);
		for (Long classpk : classpks) {
			JournalArticle journal = journalArticleService.getJournalArticleByDate(todayDate, classpk);
			if (journal != null)
				entities.add(journal);
		}
		return entities;
	}

	private List<JournalArticle> getEntities(JSONObject resultJson, Long calssTypeId, CategoryType categoryType, String egdate) {

		List<JournalArticle> entities = new ArrayList<JournalArticle>();
		for (JournalArticle journal : getJournalObjects(calssTypeId)) {
			if (journal != null) {
				switch (categoryType) {
				case ANNOUNCEMENT:
					entities.add(getJournalArticleForAnnouncement(journal));
					break;
				case TENDER:
					entities.add(parseTender(journal));
					break;
				case JOBANDVACANCY:
					entities.add(parseJobAndVacancy(journal));
					break;
				}
			}
		}

		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		entities.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < entities.size(); i++) {
			newArticles.add(stackList.pop());
		}

		return newArticles;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getAnnouncementsByLimit(@RequestHeader(value = "date") String date) {
		// 36208,
		JSONObject resultJson = new JSONObject();

		List<JournalArticle> announcements = getEntities(resultJson, Long.parseLong(36208 + ""), CategoryType.ANNOUNCEMENT, date); // announcements
		List<JournalArticle> tenders = getEntities(resultJson, Long.parseLong(85086 + ""), CategoryType.TENDER, date); // tenders
		List<JournalArticle> jobs = getEntities(resultJson, Long.parseLong(85090 + ""), CategoryType.JOBANDVACANCY, date); // jobs

		resultJson.put("announcements", announcements);
		resultJson.put("announcementCount", announcements.size());
		resultJson.put("tenders", tenders);
		resultJson.put("tenderCount", tenders.size());
		resultJson.put("jobs", jobs);
		resultJson.put("jobCount", jobs.size());
		return resultJson;

	}
}
