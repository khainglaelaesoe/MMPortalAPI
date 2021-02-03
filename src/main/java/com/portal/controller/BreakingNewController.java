package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.OrgMyanmarName;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;
import com.portal.service.JournalFolderService;

@Controller
@RequestMapping("breakingNews")
public class BreakingNewController extends AbstractController{
	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;
	
	@Autowired
	private JournalFolderService journalFolderService;
	
	@RequestMapping(value = "getBreakingNews", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getBreakingNews(@RequestHeader("Authorization") String encryptedString) {
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

		long classPK = assetEntryService.getClassPK();
		JournalArticle journal = journalArticleService.getJournalArticleByClassPK(classPK);
		List<Map<String, String>> contentlist  = new DocumentParsing().ParsingImageTextTextArea(journal.getContent());
		String title[] = new DocumentParsing().ParsingTitle(journal.getTitle());
		String englishTitle = title[0];
		String myanmarTitle = title[1];
		resultJson.put("englishTitle", englishTitle);
		resultJson.put("myanmarTitle", myanmarTitle);
		resultJson.put("contentList", contentlist);
		return resultJson;
	}
	
	@RequestMapping(value = "getBreakingNewsUpdate", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getBreakingNewsUpdate(@RequestHeader("Authorization") String encryptedString) {
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
		List<Long> classPKList = assetEntryService.getClassPKListbyCatagoryId();
		List<JournalArticle> jrList = journalArticleService.getJournalArticlebyRprimekey(classPKList);
		if(jrList.size() > 0) {
			jrList = parseBreakingNews(jrList);
		}else jrList = null;
		resultJson.put("articles", jrList);
		resultJson.put("myanmarContent", "အချက်လက်မရှိသေးပါ");
		resultJson.put("englishContent", "No Data Available");
		return resultJson;
	}
	
	public List<JournalArticle> parseBreakingNews(List<JournalArticle> jrList){
		List<JournalArticle> jrListNew = new ArrayList<JournalArticle>();
		for(JournalArticle journalArticle : jrList) {
			/* title, imageurl, location, department, date, content */
			JournalArticle newArticle = new JournalArticle();
			DocumentParsing dp = new DocumentParsing();
			System.out.println("Content_______" + journalArticle.getContent());
			String title[] = dp.ParsingTitle(journalArticle.getTitle());
			newArticle.setEngTitle(title[0]);
			newArticle.setMynamrTitle(title[1]);

			String imageUrl = "";
			imageUrl = imageUrl.isEmpty() ? getDocumentImage(journalArticle.getContent()) : imageUrl;
			newArticle.setImageUrl(imageUrl.isEmpty() ? getHttpImage(journalArticle.getContent()) : imageUrl);

			String dateString = journalArticle.getDisplaydate().split(" ")[0];
			String[] dateStr = dateString.split("-");
			String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
			newArticle.setDisplaydate(resultDateString);

			String name = journalFolderService.getNameByFolderId(Long.parseLong(journalArticle.getTreepath().split("/")[1]));
			System.out.println("Beaking News Name________________________" + name);
			if (name.equals("News and Media"))
				name = "Ministry of Information";
			if (name.equals("Union Attonery General's Office"))
				name = "Union Attonery Generals Office";
			newArticle.setEngDepartmentTitle(name);
			newArticle.setMyanmarDepartmentTitle(OrgMyanmarName.valueOf(name.replaceAll(" ", "_").replaceAll(",", "").replaceAll("-", "_")).getValue());

			String content = journalArticle.getContent();
			int index = content.indexOf("location");
			newArticle.setEngLocation(getAttribute(index, content, "en_US"));
			newArticle.setMyanmarLocation(getAttribute(index, content, "my_MM"));
			newArticle.setShareLink(getShareLinkForNews(journalArticle.getUrltitle()));

			String language = dp.AvailableLanguage(journalArticle.getContent());
			ArrayList<String> contentList = new ArrayList<String>();
			contentList = dp.ParsingAllContent(journalArticle.getContent());

			String engcontent = "";
			String myancontent = "";
			
			int i = 0;
			for (String content1 : contentList) {
				
				if (i % 2 == 0) {
					engcontent = engcontent.concat(content1);
				} else {
					myancontent = myancontent.concat(content1);
				}
				i++;
			}
			
			if (language.equals("en_US")) {
				if(engcontent.equals(""))engcontent=myancontent;
				myancontent = engcontent;
			}
			if (language.equals("my_MM")) {
				if(myancontent.equals(""))myancontent=engcontent;
				engcontent = myancontent;
			}
			
			engcontent = dp.ParsingSpan(engcontent);
			myancontent = dp.ParsingSpan(myancontent);

			if (myancontent.contains("data:image/png;base64")) {
				Document doc = Jsoup.parse(myancontent, "", Parser.htmlParser());
				Elements imgs = doc.select("img[src^=data:image/png;base64]");
				String base64String = "";
//				for (Element element : imgs) {
//					String attr = element.attr("src");
//					base64String = attr.substring(22);
//				}
//				category.setBase64image(base64String);
			}

			int start = engcontent.indexOf("<image>") + 7;
			int end = engcontent.indexOf("</image>");
			String resultString = "";
			if (start > 0 && end > 0) {
				String str = engcontent.substring(start, end);
				resultString = "<img src=" + str + "\">";
				//category.setAudioLink(str);
				newArticle.setEngContent(
						resultString + engcontent.substring(end + 8, engcontent.length()).replaceAll("<html>", "")
								.replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "")
								.replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
				newArticle.setMyanmarContent(
						resultString + myancontent.substring(end + 8, myancontent.length()).replaceAll("<html>", "")
								.replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "")
								.replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
				newArticle.setIosEngContent(Jsoup.parse(engcontent.substring(end + 8, engcontent.length())).text());
				newArticle.setIosMyaContent(Jsoup.parse(myancontent.substring(end + 8, myancontent.length())).text());
			} else {
				newArticle.setEngContent(engcontent.replaceAll("<html>", "").replaceAll("</html>", "")
						.replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "")
						.replaceAll("</body>", "").replaceAll("\n \n \n", ""));
				newArticle.setMyanmarContent(myancontent.replaceAll("<html>", "").replaceAll("</html>", "")
						.replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "")
						.replaceAll("</body>", "").replaceAll("\n \n \n", ""));

				newArticle.setIosEngContent(Jsoup.parse(engcontent).text());
				newArticle.setIosMyaContent(Jsoup.parse(myancontent).text());
			}
			newArticle.setClasspk(journalArticle.getClasspk());
			jrListNew.add(newArticle);
		}
		
		return jrListNew;
	}
	private String getShareLinkForNews(String urlTitle) {
		return "https://myanmar.gov.mm/news-media/news/latest-news/-/asset_publisher/idasset354/content/" + urlTitle.replaceAll("%", "%25");
	}
}
