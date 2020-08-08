package com.portal.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.DateUtil;
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
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private JournalFolderService journalFolderService;

	private static Logger logger = Logger.getLogger(NotiController.class);

	private JournalArticle getJournalArticleForAnnouncement(JournalArticle journalArticle) {

		/* title, imageurl, location, department, date, content */

		JournalArticle newArticle = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newArticle.setEngTitle(title[0]);
		newArticle.setMynamrTitle(title[1]);

		String imageUrl = "";
		imageUrl = imageUrl.isEmpty() ? getDocumentImage(journalArticle.getContent()) : imageUrl;
		newArticle.setImageUrl(imageUrl.isEmpty() ? getHttpImage2(journalArticle.getContent()) : imageUrl);

		String contentInfo = removeDelimeterFromContent(journalArticle.getContent());
		newArticle.setContent(ImageSourceChangeforanouncement(contentInfo.replaceAll("<span style=\"color:#0000ff;\">", "<span>").replaceAll("<span style=\"color:#050505\">", "<span>").replaceAll("<span style=\"font-size:11.5pt\">", "<span>>")).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newArticle.setDisplaydate(resultDateString);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		if (name.equals("News and Media"))
			name = "Ministry of Information";
		newArticle.setEngDepartmentTitle(name);
		newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String content = journalArticle.getContent();
		int index = content.indexOf("location");
		newArticle.setEngLocation(getAttribute(index, content, "en_US"));
		newArticle.setMyanmarLocation(getAttribute(index, content, "my_MM"));
		newArticle.setShareLink(getShareLinkForAnnouncements(journalArticle.getUrltitle()));
		return newArticle;
	}

	private List<JournalArticle> getJournalArticles(String entryListInfo) {
		List<JournalArticle> journalArticles = new ArrayList<JournalArticle>();
		for (String uuid : entryListInfo.split(",")) {
			if (!DateUtil.isEmpty(uuid)) {
				JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(uuid);
				if (journalArticle != null)
					journalArticles.add(getJournalArticleForAnnouncement(journalArticle));
			}
		}
		return journalArticles;
	}

	@RequestMapping(value = "announcement", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getAnnouncementsByLimit(@RequestParam("input") String input, @RequestParam("viewby") String viewby) {
		// 36208,
		JSONObject resultJson = new JSONObject();
		Date date = new Date();
		String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
		logger.info("todayDate !!!!!!!!!!!!!!!!!!!!!!!!!!" + todayDate);
		List<String> entryList = assetEntryService.getClassUuidByDate(Long.parseLong(36208 + ""), todayDate);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		List<JournalArticle> articles = getJournalArticles(convertEntryListToString(entryList, input));
		Stack<JournalArticle> stackList = new Stack<JournalArticle>();
		articles.forEach(article -> {
			stackList.push(article);
		});

		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		for (int i = 0; i < articles.size(); i++) {
			newArticles.add(stackList.pop());
		}

		resultJson.put("articles", newArticles);
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("totalCount", entryList.size());
		return resultJson;
	}
}
