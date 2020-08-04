package com.portal.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.MBMessage;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.Reply;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;
import com.portal.service.MessageService;

@Controller
@RequestMapping("discussion")
public class DiscussionController extends AbstractController {

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private JournalFolderService journalFolderService;

	@Autowired
	private MessageService messageService;

	private static Logger logger = Logger.getLogger(DiscussionController.class);

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();

		Document doc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(doc.children());
		List<String> titleList = removeInvalidString(Jsoup.parse(doc.toString()).text().split("/"));
		newJournal.setEngTitle(titleList.get(0));
		newJournal.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		newJournal.setEngDepartmentTitle(name);
		newJournal.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newJournal.setDisplaydate(resultDateString);

		DocumentParsing dp = new DocumentParsing();
		List<String> contentList = dp.ParsingAllContent2(journalArticle.getContent());
		newJournal.setEngContent(contentList.get(0).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "").replaceAll("<span style=\"color:#0000ff;\">", ""));
		newJournal.setMyanmarContent(contentList.get(1).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "").replaceAll("<span style=\"color:#0000ff;\">", ""));
		newJournal.setShareLink(getShareLink(journalArticle.getUrltitle()));
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

	private List<Reply> parse(List<MBMessage> messages) {
		List<Reply> replys = new ArrayList<>();
		messages.forEach(message -> {
			Reply reply = new Reply();
			reply.setBody(message.getBody());
			reply.setUsername(message.getUsername());
			reply.setCreatedate(message.getCreatedate());
			reply.setLikecount(message.getLikecount());
			reply.setSubject(message.getSubject());
		});
		return replys;
	}

	public List<JournalArticle> getJournalArticles(List<Object> entryList, String input) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		List<Object> objectList = bySize(entryList, input);
		for (Object object : objectList) {
			Object[] arr = (Object[]) object;
			List<MBMessage> messageList = messageService.byClassPK(Long.parseLong(arr[1].toString()));
			for (MBMessage msg : messageList)
				msg.setReplyList(parse(messageService.getReplyListByCommentId(msg.getMessageid())));

			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(arr[0].toString());
			if (journalArticle != null) {
				journalArticle.setMessageList(messageList);
				journalArticleList.add(journalArticle);
			}
		}
		return journalArticleList;
	}

	@RequestMapping(value = "all", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getBlogs(@RequestParam("input") String input, @RequestParam("userid") String userId) {
		JSONObject resultJson = new JSONObject();
		// classTypeId=129731;
		List<Object> entryList = assetEntryService.byClassTypeId(129739);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;
		List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList, input));
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("discussion", journalArticleList);
		resultJson.put("totalCount", entryList.size());
		return resultJson;
	}

	private List<JournalArticle> getResultList(List<Object> entryList, String input, String searchterm) {
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList, input));
		for (JournalArticle journalArticle : journalArticleList) {
			StringBuilder searchterms = new StringBuilder();
			if (journalArticle.getMyanmarDepartmentTitle() != null)
				searchterms.append(journalArticle.getMyanmarDepartmentTitle());
			if (journalArticle.getEngDepartmentTitle() != null)
				searchterms.append(journalArticle.getEngDepartmentTitle());
			if (journalArticle.getMynamrTitle() != null)
				searchterms.append(journalArticle.getMynamrTitle());
			if (journalArticle.getEngTitle() != null)
				searchterms.append(journalArticle.getEngTitle());
			if (journalArticle.getDisplaydate() != null)
				searchterms.append(journalArticle.getDisplaydate());
			if (journalArticle.getMyanmarContent() != null)
				searchterms.append(journalArticle.getMyanmarContent());
			if (journalArticle.getEngContent() != null)
				searchterms.append(journalArticle.getEngContent());
			if (searchterms.toString().contains(searchterm))
				resultList.add(journalArticle);
		}
		return resultList;
	}

	@RequestMapping(value = "bysearchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getDiscussion(@RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("userid") String userId) {
		JSONObject resultJson = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<Object> entryList = assetEntryService.byClassTypeId(129739);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		while (resultList.size() < 10 && Integer.parseInt(input) <= lastPageNo) {
			resultList.addAll(getResultList(entryList, input, searchterm));
			input = (Integer.parseInt(input) + 1) + "";
		}
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("discussion", resultList);
		resultJson.put("totalCount", journalArticleService.getCountBySearchterm(searchterm, 129739));
		return resultJson;
	}
}
