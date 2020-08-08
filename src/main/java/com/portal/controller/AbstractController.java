package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
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
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.portal.entity.AssetCategory;
import com.portal.entity.JournalArticle;
import com.portal.entity.MBMessage;
import com.portal.entity.Reply;
import com.portal.service.JournalArticleService;

@Service
public class AbstractController {

	private static Logger logger = Logger.getLogger(AbstractController.class);

	@Autowired
	private JournalArticleService journalArticleService;

	@Value("${SERVICEURL}")
	private String SERVICEURL;

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

				String imgreplace = "https://myanmar.gov.mm" + imgsrc;
				img.attr("src", imgreplace);
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
				// System.out.println("source image...." + imgsrc);
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

	public List<JournalArticle> getJournalArticles(List<String> entryList, String input, String searchTerm) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		for (String classUuid : entryList) {
			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuIdAndSearchTerm(classUuid, searchTerm);
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
		int startIndex = articleContent.lastIndexOf("[CDATA[") + 7;
		String first = articleContent.substring(startIndex, articleContent.length() - 1);
		int end = first.lastIndexOf("</p>");
		int endIndex = end < 0 ? first.indexOf("]]") : end + 4;
		return first.substring(0, endIndex);
	}

}
