package com.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		//String name = journalFolderService.getNameByFolderId(journalArticle.getFolderid());
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

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList)
			newJournalList.add(parseJournalArticle(journalArticle));
		return newJournalList;
	}

	private List<Reply> parse(List<MBMessage> messageList, String userId) {
		List<Reply> replyList = new ArrayList<Reply>();
		messageList.forEach(message -> {
			Reply reply = new Reply();
			MobileResult json = getMbData(message.getMessageid(),userId,message.getParentmessageid()); 
			logger.info("Reply____________________" + message.getParentmessageid());
			String checklikemb =json.getChecklike();
			if(checklikemb == "0.0") {
				if(messageService.likebyuserid(message.getMessageid(),json.getWebuserid(),1)) {//check web like
					checklikemb = "1.0";
				}else if(messageService.likebyuserid(message.getMessageid(),json.getWebuserid(),0)) {//check web dislike
					checklikemb = "2.0";
				}
			}
			logger.info("CheckLike____________" + checklikemb);
			long likecount=json.getLikecount();
			long totallikecount = message.getLikecount() + likecount;
			reply.setChecklike(checklikemb);
			reply.setMessageid(message.getMessageid());
			reply.setUserid(message.getUserid());
			reply.setUsername(message.getUsername());
			reply.setBody(message.getBody());
			reply.setSubject(message.getSubject());
			reply.setLikecount(totallikecount);
			reply.setDislikecount(json.getDislikecount());
			reply.setCreatedate(message.getCreatedate());
			reply.setParentmessageid(message.getParentmessageid());

			if (reply.getUserid() == Long.parseLong(userId))
				reply.setEditPermission("Yes");
			else
				reply.setEditPermission("No");
			replyList.add(reply);
		});
		return replyList;
	}

	public List<JournalArticle> getArticles(List<Object> entryList, String input, String userId) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		List<Object> objectList = bySize(entryList, input);
		ObjectMapper mapper = new ObjectMapper();

		for (Object object : objectList) {
			Object[] arr = (Object[]) object;
			List<MBMessage> messageList = new ArrayList<MBMessage>();
			List<MBMessage> webComments = messageService.byClassPK(Long.parseLong(arr[1].toString()));
			List<MBMessage> mobileComments = mapper.convertValue(getMobileComments(arr[1].toString()), new TypeReference<List<MBMessage>>() {
			});
			messageList.addAll(webComments);
			messageList.addAll(mobileComments);

			for (MBMessage msg : messageList) {
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
				
				MobileResult json = getMbData(msg.getMessageid(),userId,0); 
				String checklikemb = json.getChecklike();
				if(checklikemb == "0.0") {
					if(messageService.likebyuserid(msg.getMessageid(),json.getWebuserid(),1)) {//check web like
						checklikemb = "1.0";
					}else if(messageService.likebyuserid(msg.getMessageid(),json.getWebuserid(),0)) {//check web dislike
						checklikemb = "2.0";
					}
				}
				long likecount=json.getLikecount();
				long totallikecount = msg.getLikecount() + likecount;
				msg.setLikecount(totallikecount);
				msg.setDislikecount(json.getDislikecount());
				msg.setChecklike(checklikemb);
				logger.info(json);
			}
			

			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(arr[0].toString());
			if (journalArticle != null) {
				journalArticle.setMessageList(messageList);
				journalArticle.setpKString(arr[1].toString());
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
		List<Object> entryList = assetEntryService.byClassTypeId(129731);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		List<JournalArticle> journalArticleList = parseJournalArticleList(getArticles(entryList, input,userId));
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("blog", journalArticleList);
		resultJson.put("totalCount", entryList.size());
		return resultJson;
	}

	private List<JournalArticle> getResultList(List<Object> entryList, String input, String searchterm, String userId) {
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<JournalArticle> journalArticleList = parseJournalArticleList(getArticles(entryList, input, userId));

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
	public JSONObject getBlogs(@RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("userid") String userId) {
		JSONObject resultJson = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<Object> entryList = assetEntryService.byClassTypeId(129731);
		int lastPageNo = entryList.size() % 10 == 0 ? entryList.size() / 10 : entryList.size() / 10 + 1;

		while (resultList.size() < 10 && Integer.parseInt(input) < lastPageNo) {
			resultList.addAll(getResultList(entryList, input, searchterm, userId));
			input = (Integer.parseInt(input) + 1) + "";
		}
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("blog", resultList);
		resultJson.put("totalCount", journalArticleService.getAllBySearchterm(searchterm, 129731));
		return resultJson;
	    }

	
	@RequestMapping(value = "likecount", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public String getlikecount(@RequestParam("messageid") String messageid) {
		String likecount = messageService.likeCount(Long.parseLong(messageid)) + "" ;
		return likecount;
	}
}
