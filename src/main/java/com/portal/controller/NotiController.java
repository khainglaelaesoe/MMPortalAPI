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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.entity.AES;
import com.portal.entity.CategoryType;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.MBMessage;
import com.portal.entity.MobileResult;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.RequestVote;
import com.portal.entity.Views;
import com.portal.entity.VisibleStatus;
import com.portal.parsing.DocumentParsing;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;
import com.portal.service.MessageService;

@Controller
@RequestMapping("noti")
public class NotiController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private JournalFolderService journalFolderService;

	@Autowired
	private MessageService messageService;

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
			JournalArticle journal = journalArticleService.byClassPKAndDate(todayDate, classpk);
			if (journal != null)
				entities.add(journal);
		}
		return entities;
	}

	private List<JournalArticle> getEntities(JSONObject resultJson, Long calssTypeId, CategoryType categoryType) {

		List<JournalArticle> entities = new ArrayList<JournalArticle>();
		for (JournalArticle journal : getJournalObjects(calssTypeId)) { /* today date */
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

	@RequestMapping(value = "count", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getCount(@RequestHeader("Authorization") String encryptedString, @RequestHeader(value = "userid") String userid) {
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
		// 36208,
		JSONObject resultJson = new JSONObject();
		Long commentcount = getCommentCount(userid);
		Long blogcount = getBlogCount(userid);
		resultJson.put("announcementCount", getJournalObjects(Long.parseLong(36208 + "")).size());
		resultJson.put("tenderCount", getJournalObjects(Long.parseLong(85086 + "")).size());
		resultJson.put("jobCount", getJournalObjects(Long.parseLong(85090 + "")).size());
		resultJson.put("commentCount", commentcount);
		resultJson.put("blogCount", blogcount);
		return resultJson;
	}

	@RequestMapping(value = "announcements", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getAnnouncements(@RequestHeader("Authorization") String encryptedString) {
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
		resultJson.put("announcements", getEntities(resultJson, Long.parseLong(36208 + ""), CategoryType.ANNOUNCEMENT));
		return resultJson;
	}

	@RequestMapping(value = "tenders", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getTenders(@RequestHeader("Authorization") String encryptedString) {
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
		resultJson.put("tenders", getEntities(resultJson, Long.parseLong(85086 + ""), CategoryType.TENDER));
		return resultJson;
	}

	@RequestMapping(value = "jobs", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getJobs(@RequestHeader("Authorization") String encryptedString) {
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
		resultJson.put("jobs", getEntities(resultJson, Long.parseLong(85090 + ""), CategoryType.JOBANDVACANCY));
		return resultJson;
	}

	@RequestMapping(value = "comments", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getReplies(@RequestHeader("Authorization") String encryptedString, @RequestHeader(value = "userid") String userid, @RequestHeader(value = "messageid") String messageid) {
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
		RequestVote notidata = getReplyList(userid);
		resultJson.put("comments", notidata.getMbmessagelist());
		return resultJson;
	}

	@RequestMapping(value = "blogs", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getComments(@RequestHeader("Authorization") String encryptedString, @RequestHeader(value = "userid") String userid, @RequestHeader(value = "classpk") String classpk, @RequestHeader(value = "messageid") String messageid) {
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
		resultJson.put("blogs", parseJournalArticle(getArticle(classpk, userid)));
		resultJson.put("message", setVisibleStatus(messageid));
		return resultJson;
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getAnnouncementsByLimit(@RequestHeader("Authorization") String encryptedString, @RequestHeader(value = "date") String date, @RequestHeader(value = "userid") String userid) {
		// 36208,
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

		RequestVote notidata = getReplyList(userid);
		RequestVote blog = getBlogs(userid);
		List<JournalArticle> announcements = getEntities(resultJson, Long.parseLong(36208 + ""), CategoryType.ANNOUNCEMENT); // announcements
		List<JournalArticle> tenders = getEntities(resultJson, Long.parseLong(85086 + ""), CategoryType.TENDER); // tenders
		List<JournalArticle> jobs = getEntities(resultJson, Long.parseLong(85090 + ""), CategoryType.JOBANDVACANCY); // jobs
		resultJson.put("announcements", announcements);
		resultJson.put("announcementCount", announcements.size());
		resultJson.put("tenders", tenders);
		resultJson.put("tenderCount", tenders.size());
		resultJson.put("jobs", jobs);
		resultJson.put("jobCount", jobs.size());
		resultJson.put("comments", notidata.getMbmessagelist());
		resultJson.put("commentCount", notidata.getTotalNotiCount());
		resultJson.put("blog", blog.getJournalArticle());
		resultJson.put("blogCount", blog.getTotalNotiCount());
		return resultJson;
	}

	public RequestVote getReplyList(String userId) {
		RequestVote notidata = getNotificationList(userId);
		List<Long> messageid = notidata.getMessageid();
		List<MBMessage> mbmessageList = new ArrayList<MBMessage>();
		ObjectMapper mapper = new ObjectMapper();
		List<MBMessage> webComments = messageService.byClassPKbymessageid(messageid);
		List<MBMessage> mobileComments = mapper.convertValue(getMobileCommentsbymessageid(messageid), new TypeReference<List<MBMessage>>() {
		});
		mbmessageList.addAll(webComments);
		mbmessageList.addAll(mobileComments);

		for (MBMessage msg : mbmessageList) {
			if (msg.getMessageid() < 0)
				continue;

			if (msg.getUserid() == Long.parseLong(userId))
				msg.setEditPermission("Yes");
			else
				msg.setEditPermission("No");
			msg.getReplyList().addAll(parse(messageService.getReplyListByCommentId(msg.getMessageid()), userId));

			List<MBMessage> replyList = mapper.convertValue(getMobileReplyList(msg.getMessageid() + ""), new TypeReference<List<MBMessage>>() {
			});
			msg.getReplyList().addAll(parse(replyList, userId));

			MobileResult json = getMbData(msg.getMessageid(), userId, 0);
			String checklikemb = json.getChecklike();
			if (checklikemb == "0.0") {
				if (messageService.likebyuserid(msg.getMessageid(), json.getWebuserid(), 1)) {// check web like
					checklikemb = "1.0";
				} else if (messageService.likebyuserid(msg.getMessageid(), json.getWebuserid(), 0)) {// check web dislike
					checklikemb = "2.0";
				}
			}

			long likecount = json.getLikecount();
			long totallikecount = msg.getLikecount() + likecount;
			msg.setLikecount(totallikecount);
			msg.setDislikecount(json.getDislikecount());
			msg.setChecklike(checklikemb);
		}
		notidata.setMbmessagelist(mbmessageList);
		return notidata;
	}

	public RequestVote getBlogs(String userId) {
		List<JournalArticle> newArticles = new ArrayList<JournalArticle>();
		List<Long> classPKList = new ArrayList<Long>();
		RequestVote res = getBlogUserbyid(userId);
		classPKList = res.getClasspklist();
		if (classPKList.size() > 0) {
			List<JournalArticle> journalArticleList = parseJournalArticleList(getArticles(classPKList, "1", userId));
			Stack<JournalArticle> stackList = new Stack<JournalArticle>();
			journalArticleList.forEach(article -> {
				stackList.push(article);
			});

			for (int i = 0; i < journalArticleList.size(); i++) {
				newArticles.add(stackList.pop());
			}
		}
		res.setJournalArticle(newArticles);
		return res;
	}

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {

		/* title, department title, content detail */

		if (journalArticle == null)
			return null;

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

		List<String> contentList = dp.ParsingAllContent(journalArticle.getContent());
		newJournal.setEngContent(contentList.get(0).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
		newJournal.setMyanmarContent(contentList.get(1).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));

		newJournal.setShareLink(getShareLink(journalArticle.getUrltitle()));
		newJournal.setMessageList(journalArticle.getMessageList());
		newJournal.setpKString(journalArticle.getpKString());
		return newJournal;
	}

	private String getShareLink(String urlTitle) {
		return "https://myanmar.gov.mm/blogs/-/asset_publisher/m9WiUYPkhQIm/content/" + urlTitle.replaceAll("%", "%25");
	}

}
