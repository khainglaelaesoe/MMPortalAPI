package com.portal.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.entity.AssetCategory;
import com.portal.entity.CategoryType;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.MBMessage;
import com.portal.entity.MobileResult;
import com.portal.entity.OrgEngName;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.PollsChoice;
import com.portal.entity.Reply;
import com.portal.entity.RequestVote;
import com.portal.entity.TopicEngName;
import com.portal.entity.User_;
import com.portal.entity.ViewBy;
import com.portal.entity.mobileuser;
import com.portal.parsing.DocumentParsing;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;
import com.portal.service.MessageService;

@Service
public class AbstractController {

	private static Logger logger = Logger.getLogger(AbstractController.class);

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private JournalFolderService journalFolderService;

	@Autowired
	private MessageService messageService;

	// public final String secretKey = "7M8N3SLQ8QIKDJOSEPXJKJDFOZIN1NBOVWMMGIVA";

	public final String secretKey = "7M8N3SLQ8QIKDJOSEPXJKJDFOZIN1NBO";

	@Value("${SERVICEURL}")
	private String SERVICEURL;

	public boolean isValidPaganation(String input) {
		return input.length() < 6;
	}

	public boolean isValidTopic(String topicKey) {
		for (TopicEngName topic : TopicEngName.values()) {
			if (topic.toString().contains(topicKey) || topicKey.toString().equals("all")) {
				return true;
			}
		}
		return false;
	}

	public boolean isValidOrgName(String name) {
		for (OrgEngName orgName : OrgEngName.values()) {
			if (orgName.toString().equals(name))
				return true;
		}
		return false;
	}

	public boolean isValidSearchTerm(String searchTerm) {
		return searchTerm.length() < 100;
	}

	public boolean isValidViewBy(String viewBy) {
		try {
			ViewBy viewByEnum = ViewBy.valueOf(viewBy.toUpperCase());
			return ViewBy.CLOSINGDATE == viewByEnum || ViewBy.LATEST == viewByEnum || ViewBy.MOSTVIEW == viewByEnum;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isValidCategoryType(String categoryType) {
		for (CategoryType eachCategory : CategoryType.values()) {
			logger.info("categoryType: " + categoryType);
			if (eachCategory.toString().equals(categoryType.trim().toUpperCase())) {
				logger.info("categoryType: " + categoryType.trim().toUpperCase());
				logger.info("categoryType: " + eachCategory);

				return true;
			}
		}
		return false;
	}

	public boolean isAuthorize(String decryptedString) {
		/*
		 * Apple deviceID - 98F7A963-33DF-4C49-AC80-F35F870BFAAB Android DeviceID -
		 * 25f0cee94f08e5c2
		 */
		return decryptedString.contains("Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");
	}

	public String getMyanmarElement(String content, String element, String remover) {
		int begin = content.indexOf(element);
		content = content.substring(begin, content.length());
		int end = content.indexOf("</dynamic-content>");
		String remainString = content.substring(end, content.length());

		int mStart = remainString.indexOf(remover);
		if (mStart > 0) {
			int mEnd = remainString.indexOf("]]");
			if (!remainString.isEmpty() && mEnd > 0)
				return Jsoup.parse(remainString.substring(mStart, mEnd)).text();
		}
		return "";
	}

	public String getEngElement(String content, String element, String remover) {
		int begin = content.indexOf(element);
		String remainString = content.substring(begin, content.length());
		int mStart = remainString.indexOf(remover);
		if (mStart > 0) {
			int mEnd = remainString.indexOf("]]");
			if (remainString.isEmpty() || mEnd < 0 || mStart < 0 || mEnd < mStart)
				return "";

			return Jsoup.parse(remainString.substring(mStart, mEnd)).text();
		}
		return "";
	}

	public String ImageSourceChange(String htmlinput) {

		Document docimage = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		Elements images = docimage.getElementsByTag("img");
		if (images.size() > 0) {
			for (Element img : images) {
				String imgsrc = img.attr("src");
				String imgreplace = !imgsrc.startsWith("http") && !imgsrc.contains("base64") ? "https://myanmar.gov.mm" + imgsrc : imgsrc;
				img.attr("src", imgreplace);
			}
		}
		return docimage.html();
	}

	public String ImageSourceChange2(String htmlinput) {

		Document docimage = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		Elements images = docimage.getElementsByTag("img");
		if (images.size() > 0) {
			for (Element img : images) {
				String imgsrc = img.attr("src");
				String imgreplace = !imgsrc.startsWith("http") && !imgsrc.contains("base64") && !imgsrc.startsWith("www") ? imgsrc.startsWith("/sites") ? "https://www.moi.gov.mm" + imgsrc : "https://myanmar.gov.mm" + imgsrc : imgsrc;
				img.attr("src", imgreplace);

				if (imgsrc.contains("base64")) {
					byte[] decode = Base64.getDecoder().decode(imgsrc);
					imgsrc = decode.toString();
				}

			}
		}

		Elements links = docimage.getElementsByTag("a");
		if (links.size() > 0) {
			for (Element link : links) {
				String imgsrc = link.attr("href");
				if (!imgsrc.startsWith("http") && !imgsrc.contains("base64")) {
					String imgreplace = !imgsrc.startsWith("http") && !imgsrc.contains("base64") && !imgsrc.startsWith("www") ? imgsrc.startsWith("/sites") ? "https://www.moi.gov.mm" + imgsrc : "https://myanmar.gov.mm" + imgsrc : imgsrc;
					link.attr("href", imgreplace);
				}
			}
		}
		return docimage.html();
	}

	public String ImageSourceChangeforanouncement(String htmlinput) {

		Document docimage = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		Elements images = docimage.getElementsByTag("img");
		String imgreplace;
		if (images.size() > 0) {
			for (Element img : images) {
				String imgsrc = img.attr("src");
				if (img.attr("src").contains("data:image/jpeg;")) {
					imgreplace = imgsrc;
				} else if (img.attr("src").contains("https://")) {
					imgreplace = imgsrc;
				} else {
					imgreplace = "https://myanmar.gov.mm" + imgsrc;
				}
				img.attr("src", imgreplace);
			}
		}
		return docimage.html();
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

	public List<String> removeInvalidString(String[] titleArr) {
		List<String> titleList = new ArrayList<String>();
		for (String title : titleArr) {
			if (title != null && !title.isEmpty() && title.length() > 1)
				titleList.add(title);
		}
		return titleList;
	}

	public List<String> removeDelimeterFrom(String str) {
		Document doc = Jsoup.parse(str);
		replaceTag(doc.children());
		String[] infos = Jsoup.parse(doc.toString()).text().split("/");
		return removeInvalidString(infos);
	}

	public String getImage(String content) {
		int start = content.indexOf("/image/");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("<");
		return remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
	}

	public String getDocumentImage(String content) {
		int start = content.indexOf("/document");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("]]");
		return remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
	}

	public String getDocumentImage2(String content) {
		int start = content.indexOf("/document");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("]]");
		return remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
	}

	public String getJournalImage(String content) {
		int start = content.indexOf("/image");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("<");
		return remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
	}

	public String getHttpImage(String content) {
		int start = content.indexOf("http");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("]]");
		return remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
	}

	public String getHttpImage2(String content) {
		int start = content.indexOf("http");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf("\"");
		return remainString.substring(0, end).startsWith("/") ? "https://myanmar.gov.mm" + remainString.substring(0, end) : remainString.substring(0, end);
	}

	public String convertEntryListToString(List<String> entryList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (entryList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++)
			info += entryList.get(i) + ",";
		return info;
	}

	public String getMyanamrContent(String content) {
		int begin = content.indexOf("\"text_area\"");
		content = content.substring(begin, content.length());
		int end = content.indexOf("</dynamic-content>");
		String remainString = content.substring(end, content.length());

		int mStart = remainString.indexOf("<dynamic-content language-id=\"my_MM\">");
		if (mStart > 0) {
			int mEnd = remainString.lastIndexOf("</dynamic-content>");
			return Jsoup.parse(remainString.substring(mStart, mEnd)).text().replaceAll("value 1", "");
		}
		return "";
	}

	public String getEngContent(String content) {
		int begin = content.indexOf("\"text_area\"");
		if (begin < 0)
			return "";
		content = content.substring(begin, content.length());

		int start = content.indexOf("<dynamic-content language-id=\"en_US\">");
		int end = content.indexOf("</dynamic-content>");

		if (start < 0 || end < 0)
			return "";
		return Jsoup.parse(content.substring(start, end)).text().replaceAll("value 1", "");
	}

	public String convertObjectListToString(List<AssetCategory> entryList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (entryList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++) {
			info += entryList.get(i).toString() + ",";
		}
		return info;
	}

	public List<JournalArticle> byPaganation(List<JournalArticle> journalList, String input) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		int index = Integer.parseInt(input);
		int lastIndex = (journalList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;
		for (int i = startIndex; i <= lastIndex; i++)
			newJournalList.add(journalList.get(i));
		return newJournalList;
	}

	public List<Object> bySize(List<Object> entryList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (entryList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		List<Object> objectList = new ArrayList<Object>();
		for (int i = startIndex; i <= lastIndex; i++)
			objectList.add(entryList.get(i));
		return objectList;
	}

	public List<MBMessage> getMobileComments(String classPK) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("classpk", classPK);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/comment/mobile";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<List> response = null;
		try {

			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, List.class);
			logger.info("response.getBody()!!!!!!!!!!!!!!:" + response.getBody());
			List<MBMessage> userScores = response.getBody();
			logger.info("LeaveBalance list size:" + userScores.size());
			return userScores;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new ArrayList<MBMessage>();
	}

	public List<JournalArticle> getJournalArticlesBySearchTerm(List<Long> classpks, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		for (Long classPK : classpks) {
			JournalArticle journalArticle = journalArticleService.byClassPKAndSearchTerms(classPK, searchTerm);
			if (journalArticle != null)
				journalArticleList.add(journalArticle);
		}
		return journalArticleList;
	}

	public List<String> getRatingsEntry(String classNameId, String classPk) {

		// Prepare the header
		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(acceptableMediaTypes);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("classnameid", classNameId);
		headers.add("classpk", classPk);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		// Prepare the URL
		String url = SERVICEURL + "/user/getratingsentry";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		// RESTTemplate to call the service
		RestTemplate restTemplate = new RestTemplate();

		// Data type for response
		HttpEntity<List> response = null;
		try {

			restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, List.class);

			List<String> userScores = response.getBody();
			logger.info("LeaveBalance list size:" + userScores.size());

			return userScores;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new ArrayList<String>();
	}

	public String getWebUserId(String userId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("userid", userId);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/user/webuserid";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = null;
		try {

			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, String.class);
			logger.info("response.getBody()!!!!!!!!!!!!!!:" + response.getBody());
			return response.getBody();

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return null;
	}

	public String getShareLinkForAnnouncements(String urlTitle) {
		return "https://myanmar.gov.mm/news-media/announcements/-/asset_publisher/idasset291/content/" + urlTitle.replaceAll("%", "%25");
	}

	public String getAttribute(int index, String content, String remover) {
		if (index < 0)
			return "";

		String remainString = content.substring(index, content.length());
		int start = remainString.indexOf(remover);
		if (start < 0)
			return "";

		String remainString2 = remainString.substring(start, remainString.length());
		int startIndex = remainString2.indexOf("CDATA[") + 6;
		int endIndex = remainString2.indexOf("]]");
		String result = remainString2.substring(startIndex, endIndex);

		if (result.isEmpty()) {
			String remainString3 = content.substring(endIndex, remainString2.length());
			int start2 = remainString3.indexOf(remover);
			String remainString4 = remainString3.substring(start2, remainString3.length());
			int startIndex2 = remainString4.indexOf("CDATA[") + 6;
			int endIndex2 = remainString4.indexOf("]]");

			if (start2 < 0 || startIndex2 < 0 || endIndex2 < 0)
				return "";

			result = remainString4.substring(startIndex2, endIndex2);
		}

		return result.startsWith("/") ? "https://myanmar.gov.mm" + result : result;
	}

	public List<MBMessage> getMobileReplyList(String messageId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("messageid", messageId);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/comment/reply";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<List> response = null;
		try {

			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, List.class);
			logger.info("reply list size:" + response.getBody().size());
			return response.getBody();

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new ArrayList<MBMessage>();
	}

	public String removeDelimeterFromContent(String articleContent) {
		int startIndex = articleContent.indexOf("[CDATA[") + 7;
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.lastIndexOf("</p>");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		return first.substring(0, endIndex);
	}

	public MobileResult getMbData(long classpk, String userid, long parentmessageid) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("messageid", classpk + "");
		params.put("userid", userid);
		params.put("parentmessageid", parentmessageid + "");
		String uri = SERVICEURL + "/likeDislike/getMbData";
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri);
		for (Map.Entry<String, String> entry : params.entrySet()) {
			builder.queryParam(entry.getKey(), entry.getValue());
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");
		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<MobileResult> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entityHeader, MobileResult.class);
		System.out.println(response);
		return response.getBody();
	}

	public RequestVote getMobileVoltCount(String mbuserid, String pollOrSurveyId, long totalVoteCount, List<PollsChoice> pollslist) {
		RequestVote reqVote = new RequestVote();
		reqVote.setPollsChoiceList(pollslist);
		reqVote.setUserid(mbuserid);
		reqVote.setTotalVoteCount(totalVoteCount + "");
		reqVote.setPollOrSurveyId(pollOrSurveyId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<RequestVote> entityHeader = new HttpEntity<>(reqVote, headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/vote/getVote";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		reqVote = restTemplate.postForObject(url, entityHeader, RequestVote.class);
		return reqVote;
	}

	public RequestVote getMobileSurveyCount(String mbuserid, String pollOrSurveyId) {
		RequestVote reqVote = new RequestVote();
		reqVote.setUserid(mbuserid);
		reqVote.setPollOrSurveyId(pollOrSurveyId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<RequestVote> entityHeader = new HttpEntity<>(reqVote, headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/survey/getSurvey";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		reqVote = restTemplate.postForObject(url, entityHeader, RequestVote.class);
		return reqVote;
	}

	public String getPDFLink(String content) {
		int start = content.indexOf("href=");
		if (start < 0)
			return "";
		String remainString = content.substring(start, content.length());
		int end = remainString.indexOf(".pdf");
		if (end < 0)
			return "";
		return remainString.substring(6, end + 4);
	}

	public JournalArticle getJournalArticleForAnnouncement(JournalArticle journalArticle) {

		/* title, imageurl, location, department, date, content */
		JournalArticle newArticle = new JournalArticle();
		DocumentParsing dp = new DocumentParsing();
		String title[] = dp.ParsingTitle(journalArticle.getTitle());
		newArticle.setEngTitle(title[0]);
		newArticle.setMynamrTitle(title[1]);

		String imageUrl = "";
		imageUrl = imageUrl.isEmpty() ? getDocumentImage2(journalArticle.getContent()) : imageUrl;
		newArticle.setImageUrl(imageUrl.isEmpty() ? getHttpImage(journalArticle.getContent()) : imageUrl);
		newArticle.setImageUrl(newArticle.getImageUrl().isEmpty() ? getJournalImage(journalArticle.getContent()) : newArticle.getImageUrl());

		String engContent = getEngElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"en_US\">");
		String myaContent = getEngElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Content", "<dynamic-content language-id=\"my_MM\">");

		engContent = engContent.isEmpty() ? myaContent : engContent;
		myaContent = myaContent.isEmpty() ? engContent : myaContent;

		if (newArticle.getImageUrl() != null) {
			String[] imageUrls = newArticle.getImageUrl().split(",");
			for (String image : imageUrls) {
				logger.info("image: " + image);
				engContent = engContent + "<img src=" + image + ">";
				myaContent = myaContent + "<img src=" + image + ">";
			}
		}

		newArticle.setEngContent(ImageSourceChange2(dp.ParsingSpan(engContent)));
		newArticle.setMyanmarContent(ImageSourceChange2(dp.ParsingSpan(myaContent)));

		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newArticle.setDisplaydate(resultDateString);

		String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
		if (name.equals("News and Media"))
			name = "Ministry of Information";
		newArticle.setEngDepartmentTitle(name);
		try {
			newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());
		} catch (Exception e) {
			newArticle.setMyanmarDepartmentTitle(name);
		}

		String engLocation = getEngElement(journalArticle.getContent(), "location", "<dynamic-content language-id=\"en_US\">");
		String myaLocation = getEngElement(journalArticle.getContent(), "location", "<dynamic-content language-id=\"my_MM\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "location", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "location", "<dynamic-content language-id=\"my_MM\">");

		newArticle.setEngLocation(engLocation);
		newArticle.setMyanmarLocation(myaLocation);

		newArticle.setShareLink(getShareLinkForAnnouncements(journalArticle.getUrltitle()));
		newArticle.setCategoryType(CategoryType.ANNOUNCEMENT);
		newArticle.setContent(journalArticle.getContent());
		return newArticle;
	}

	public List<Reply> parse(List<MBMessage> messageList, String userId) {
		List<Reply> replyList = new ArrayList<Reply>();
		messageList.forEach(message -> {
			Reply reply = new Reply();
			MobileResult json = getMbData(message.getMessageid(), userId, message.getParentmessageid());
			logger.info("Reply____________________" + message.getParentmessageid());
			String checklikemb = json.getChecklike();
			if (checklikemb == "0.0") {
				if (messageService.likebyuserid(message.getMessageid(), json.getWebuserid(), 1)) {// check web like
					checklikemb = "1.0";
				} else if (messageService.likebyuserid(message.getMessageid(), json.getWebuserid(), 0)) {// check web dislike
					checklikemb = "2.0";
				}
			}
			logger.info("CheckLike____________" + checklikemb);
			long likecount = json.getLikecount();
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

	public List<MBMessage> getMobileCommentsbymessageid(List<Long> messageid) {
		RequestVote requestvote = new RequestVote();
		requestvote.setMessageid(messageid);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");
		HttpEntity<RequestVote> entityHeader = new HttpEntity<>(requestvote, headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/comment/commentbymessageid";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<List> response = null;
		try {

			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, List.class);
			logger.info("response.getBody()!!!!!!!!!!!!!!:" + response.getBody());
			List<MBMessage> userScores = response.getBody();
			logger.info("LeaveBalance list size:" + userScores.size());
			return userScores;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new ArrayList<MBMessage>();
	}

	public RequestVote getNotificationList(String userid) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("userid", userid);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/user/getNotiUpdate";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<RequestVote> response = null;
		try {
			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, RequestVote.class);
			RequestVote userScores = response.getBody();
			return userScores;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new RequestVote();
	}

	public String convertLongListToString(List<Long> articleIdList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (articleIdList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++)
			info += articleIdList.get(i) + ",";
		return info;
	}

	public String byPaganationWithArticle(List<String> articleIdList, String input) {
		int index = Integer.parseInt(input);
		int lastIndex = (articleIdList.size() - 1) - (index * 10 - 10);
		int substract = lastIndex < 9 ? lastIndex : 9;
		int startIndex = lastIndex - substract;

		String info = "";
		for (int i = startIndex; i <= lastIndex; i++)
			info += articleIdList.get(i) + ",";
		return info;
	}

	public JournalArticle getArticle(String classpk, String userId) {
		ObjectMapper mapper = new ObjectMapper();
		Long classPK = Long.parseLong(classpk.toString());
		JournalArticle journalArticle = journalArticleService.byClassPK(classPK);
		if (journalArticle != null) {
			List<MBMessage> messageList = new ArrayList<MBMessage>();
			List<MBMessage> webComments = messageService.byClassPK(classPK);
			List<MBMessage> mobileComments = mapper.convertValue(getMobileComments(classpk), new TypeReference<List<MBMessage>>() {
			});
			messageList.addAll(mobileComments);
			messageList.addAll(webComments);

			for (MBMessage msg : messageList) {
				if (msg.getMessageid() < 0)
					continue;

				if (msg.getUserid() == Long.parseLong(userId))
					msg.setEditPermission("Yes");
				else
					msg.setEditPermission("No");

				// msg.getReplyList().addAll(parse(messageService.getReplyListByCommentId(msg.getMessageid()),
				// userId));
				msg.getReplyList().addAll(parse(messageService.getReplyListByCommentId(msg.getParentmessageid()), userId));

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

			journalArticle.setMessageList(messageList);
			journalArticle.setpKString(classpk);
		}
		return journalArticle;
	}

	public JournalArticle getArticleBySearchTerm(String classpk, String userId, String searchTerm) {
		ObjectMapper mapper = new ObjectMapper();
		Long classPK = Long.parseLong(classpk.toString());
		JournalArticle journalArticle = journalArticleService.byClassPK(classPK, searchTerm);
		if (journalArticle != null) {
			List<MBMessage> messageList = new ArrayList<MBMessage>();
			List<MBMessage> webComments = messageService.byClassPK(classPK);
			List<MBMessage> mobileComments = mapper.convertValue(getMobileComments(classpk), new TypeReference<List<MBMessage>>() {
			});
			messageList.addAll(mobileComments);
			messageList.addAll(webComments);

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

			journalArticle.setMessageList(messageList);
			journalArticle.setpKString(classpk);
		}

		return journalArticle;
	}

	public List<JournalArticle> getAllArticles(List<Long> classPKList, String input, String userId) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		for (Long classpk : classPKList) {
			JournalArticle journal = getArticle(classpk.toString(), userId);
			if (journal != null) {
				journalArticleList.add(journal);
			}
		}
		return journalArticleList;
	}

	public List<JournalArticle> getArticles(List<Long> classPKList, String userId, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		for (Long classpk : classPKList) {
			JournalArticle journal = getArticleBySearchTerm(classpk.toString(), userId, searchTerm);
			if (journal != null)
				journalArticleList.add(journal);
		}
		return journalArticleList;
	}

	public RequestVote getBlogUserbyid(String userid) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("userid", userid);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/comment/getClassPK";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<RequestVote> response = null;
		try {

			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, RequestVote.class);
			logger.info("response.getBody()!!!!!!!!!!!!!!:" + response.getBody());
			RequestVote userScores = response.getBody();
			return userScores;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return new RequestVote();
	}

	public long getCommentCount(String userid) {
		long count = 0;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("userid", userid);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/user/getCommentCount";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Long> response = null;
		try {
			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, Long.class);
			count = response.getBody();
			logger.info("comment count __________" + count);
			return count;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return count;
	}

	public String getImageString(String articleContent) {
		int startIndex = articleContent.indexOf("/documents/");
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.indexOf(".jpg");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		return "https://myanmar.gov.mm" + first.substring(0, endIndex);
	}

	public String getEngDownLoadLink(String articleContent) {
		int startIndex = articleContent.lastIndexOf("[CDATA[") - 200;
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.lastIndexOf("</p>");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		String raw = first.substring(0, endIndex);

		int rawStart = raw.indexOf("/documents/");
		if (rawStart < 0)
			return "";
		return "https://myanmar.gov.mm" + raw.substring(rawStart, raw.length());
	}

	public long getBlogCount(String userid) {
		long count = 0;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("userid", userid);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/user/getblogCount";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Long> response = null;
		try {
			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, Long.class);
			count = response.getBody();
			logger.info("Blog count __________" + count);
			return count;

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return count;
	}

	public String setVisibleStatus(String messageid) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("messageid", messageid);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<String> entityHeader = new HttpEntity<String>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/comment/changeStatus";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> response = null;
		try {
			response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, String.class);

		} catch (Exception e) {
			logger.error("ERRROR is - " + e.getMessage() + ", " + response);
		}
		return response.getBody();
	}

	public String saveUser(User_ user) {
		mobileuser mbuser = new mobileuser();
		mbuser = convertMobileUser(user);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic bXlhbnBvcnRhbDptWUBubWFAcnAwcnRhbA==");

		HttpEntity<mobileuser> entityHeader = new HttpEntity<>(mbuser, headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/mbuser/saveuser";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);

		RestTemplate restTemplate = new RestTemplate();
		JSONObject res = restTemplate.postForObject(url, entityHeader, JSONObject.class);
		logger.info(res.get("message").toString());
		return res.get("message").toString();
	}

	public mobileuser convertMobileUser(User_ user) {
		mobileuser mbuser = new mobileuser();
		try {
			mbuser.setWebuserid(user.getUserid());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
			mbuser.setCreatedate(formatter.parse(user.getCreatedate()));
			mbuser.setModifieddate(formatter.parse(user.getModifieddate()));
			mbuser.setScreenname(user.getScreenname());
			mbuser.setEmailaddress(user.getEmailaddress());
			mbuser.setFacebookid(user.getFacebookid());
			mbuser.setName(user.getFirstname());
			mbuser.setPhoneno(user.getPhone());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return mbuser;

	}

}
