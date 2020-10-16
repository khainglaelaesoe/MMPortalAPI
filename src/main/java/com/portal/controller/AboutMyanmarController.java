package com.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base32;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
import com.portal.entity.AboutMyanmarType;
import com.portal.entity.AssetCategory;
import com.portal.entity.AssetCategoryProperty;
import com.portal.entity.AssetEntry;
import com.portal.entity.JournalArticle;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetCategoryPropertyService;
import com.portal.service.AssetCategoryService;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("aboutmyanmar")
public class AboutMyanmarController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetCategoryService assetCategoryService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private AssetCategoryPropertyService assetCategoryPropertyService;

	private static Logger logger = Logger.getLogger(AboutMyanmarController.class);

	// HISTORY, TRAVEL, PEOPLE, GEOGRAPHY, CULTURE, ECONOMY, SYMBOL,
	// DESTINATION,FESTIVAL

	private List<String> getLink(int index, String content, String remover) {
		// System.out.println("content link....."+content);
		List<String> stringList = new ArrayList<String>();
		if (index < 0)
			return stringList;

		String remainString = content.substring(index, content.length());
		int start = remainString.indexOf(remover);
		String remainString2 = remainString.substring(start, remainString.length());
		int startIndex = remainString2.indexOf("CDATA[") + 6;
		int endIndex = remainString2.indexOf("]]");
		if (startIndex < 0 || endIndex < 0)
			return stringList;

		String result = remainString2.substring(startIndex, endIndex);
		stringList.add(result);
		stringList.add(remainString2);
		stringList.add(endIndex + "");
		return stringList;
	}

	private List<String> getNameList(String indexString, String content, String remover) {
		List<String> attributeList = new ArrayList<String>();
		int index = content.indexOf(indexString);
		if (index > 0) {
			// System.out.println("content....."+content);
			List<String> first = getLink(index, content, remover);
			if (CollectionUtils.isEmpty(first))
				return attributeList;

			attributeList.add(first.get(0));

			String remainString = first.get(1).substring(Integer.parseInt(first.get(2)), first.get(1).length());
			index = remainString.indexOf(indexString);

			if (index < 0)
				return attributeList;
			first = getLink(index, remainString, remover);
			attributeList.add(first.get(0));

			remainString = first.get(1).substring(Integer.parseInt(first.get(2)), first.get(1).length());
			index = remainString.indexOf(indexString);
			if (index < 0)
				return attributeList;
			first = getLink(index, remainString, remover);
			attributeList.add(first.get(0));
		}
		return attributeList;
	}

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();
		Document doc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(doc.children());
		List<String> titleList = removeInvalidString(Jsoup.parse(doc.toString()).text().split("/"));
		newJournal.setEngTitle(titleList.get(0));
		newJournal.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));

		String jacontent = journalArticle.getContent();

		ArrayList<String> contentList = new ArrayList<String>();
		DocumentParsing dp = new DocumentParsing();
		contentList = dp.ParsingAllContent(jacontent);

		String engcontent = "";
		String myancontent = "";

		int i = 0;
		for (String content : contentList) {
			if (i % 2 == 0) {
				engcontent = engcontent.concat(content);
			} else {
				myancontent = myancontent.concat(content);
			}
			i++;
		}

		int start = engcontent.indexOf("<audio>") + 7;
		int end = engcontent.indexOf("</audio>");
		String resultString = "";
		if (start > 0 && end > 0) {
			String str = engcontent.substring(start, end);
			resultString = "<audio controls><source src=" + str + " type=\"audio/mpeg\">";
			newJournal.setEngContent(resultString + engcontent.substring(end, engcontent.length()));
			newJournal.setMyanmarContent(resultString + myancontent.substring(end, myancontent.length()));
			newJournal.setIosEngContent(Jsoup.parse(engcontent.substring(end, engcontent.length())).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent.substring(end, myancontent.length())).text());
		} else {
			newJournal.setEngContent(engcontent);
			newJournal.setMyanmarContent(myancontent);
			newJournal.setIosEngContent(Jsoup.parse(engcontent).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent).text());
		}
		return newJournal;
	}

	private JournalArticle parseJournalArticleforDestination(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();
		Document doc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(doc.children());
		List<String> titleList = removeInvalidString(Jsoup.parse(doc.toString()).text().split("/"));
		newJournal.setEngTitle(titleList.get(0));
		newJournal.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));

		String jacontent = journalArticle.getContent();
		newJournal.setImageUrl(getImage(jacontent));

		DocumentParsing dp = new DocumentParsing();
		String[] contentList = dp.ParsingContent(jacontent);

		String engcontent = contentList[0];
		String myancontent = contentList[1];
		if (engcontent == null)
			engcontent = myancontent;
		if (myancontent == null)
			myancontent = engcontent;
		int start = engcontent.indexOf("<audio>") + 7;
		int end = engcontent.indexOf("</audio>");
		String resultString = "";
		if (start > 0 && end > 0) {
			String str = engcontent.substring(start, end);
			resultString = "<audio controls><source src=" + str + " type=\"audio/mpeg\">";
			newJournal.setEngContent(resultString + engcontent.substring(end, engcontent.length()));
			newJournal.setMyanmarContent(resultString + myancontent.substring(end, myancontent.length()));
			newJournal.setIosEngContent(Jsoup.parse(engcontent.substring(end, engcontent.length())).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent.substring(end, myancontent.length())).text());
		} else {
			newJournal.setEngContent(engcontent);
			newJournal.setMyanmarContent(myancontent);
			newJournal.setIosEngContent(Jsoup.parse(engcontent).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent).text());
		}
		return newJournal;
	}

	private JournalArticle parseJournalArticleforVisa(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();
		Document doc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(doc.children());
		List<String> titleList = removeInvalidString(Jsoup.parse(doc.toString()).text().split("/"));
		newJournal.setEngTitle(titleList.get(0));
		newJournal.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));

		// newJournal.setMyanmarContent(getMyanamrContent(journalArticle.getContent()));
		// newJournal.setEngContent(getEngContent(journalArticle.getContent()));
		String jacontent = journalArticle.getContent();

		newJournal.setImageUrl(getImage(jacontent));
		ArrayList<String> contentList = new ArrayList<String>();
		DocumentParsing dp = new DocumentParsing();
		List<String> engimage = dp.ParsingEngImage(jacontent);
		List<String> myanimage = dp.ParsingMyanImage(jacontent);
		String engimages = "";
		String myanimages = "";
		for (String eng : engimage)
			engimages += eng;
		for (String myan : myanimage)
			myanimages += myan;
		newJournal.setEngImageUrl(engimages);
		newJournal.setMyanamrImageUrl(myanimages);
		contentList = dp.ParsingAllContent(jacontent);

		String engcontent = "";
		String myancontent = "";

		int i = 0;
		for (String content : contentList) {
			if (i % 2 == 0) {
				engcontent = engcontent.concat(content);

			} else {
				myancontent = myancontent.concat(content);
			}
			i++;
		}

		int end = engcontent.indexOf("<html>");
		String resultString = "";
		if (end > 0) {
			System.out.println("end...." + end);
			newJournal.setEngContent(resultString + engcontent.substring(end, engcontent.length()));
			newJournal.setMyanmarContent(resultString + myancontent.substring(end, myancontent.length()));
			newJournal.setIosEngContent(Jsoup.parse(engcontent.substring(end, engcontent.length())).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent.substring(end, myancontent.length())).text());
		} else {
			newJournal.setEngContent(engcontent);
			newJournal.setMyanmarContent(myancontent);
			newJournal.setIosEngContent(Jsoup.parse(engcontent).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent).text());
		}

		List<String> myanmarList = getNameList("Name", jacontent, "my_MM");
		if (!CollectionUtils.isEmpty(myanmarList)) {
			newJournal.setmNameList(myanmarList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : myanmarList)
				hashlist.put(index++, list);
			newJournal.setiOSmNameList(hashlist);
		}

		List<String> engList = getNameList("Name", jacontent, "en_US");
		if (!CollectionUtils.isEmpty(engList)) {
			newJournal.seteNameList(engList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : engList)
				hashlist.put(index++, list);
			newJournal.setiOSeNameList(hashlist);
		}

		List<String> myanmarLinkList = getNameList("Link", jacontent, "my_MM");
		if (!CollectionUtils.isEmpty(myanmarLinkList)) {
			newJournal.setmLinkList(myanmarLinkList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : myanmarLinkList)
				hashlist.put(index++, list);
			newJournal.setiOSmLinkList(hashlist);
		}

		List<String> engLinkList = getNameList("Link", jacontent, "en_US");
		if (!CollectionUtils.isEmpty(engLinkList)) {
			newJournal.seteLinkList(engLinkList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : engLinkList)
				hashlist.put(index++, list);
			newJournal.setiOSeLinkList(hashlist);
		}

		return newJournal;
	}

	private JournalArticle parseJournalArticleforEmbassy(JournalArticle journalArticle) {
		/* title, department title, content detail */

		JournalArticle newJournal = new JournalArticle();
		Document doc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(doc.children());
		List<String> titleList = removeInvalidString(Jsoup.parse(doc.toString()).text().split("/"));
		newJournal.setEngTitle(titleList.get(0));
		newJournal.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));
		newJournal.setImageUrl(getImage(journalArticle.getContent()));
		// newJournal.setMyanmarContent(getMyanamrContent(journalArticle.getContent()));
		// newJournal.setEngContent(getEngContent(journalArticle.getContent()));
		String jacontent = journalArticle.getContent();
		System.out.println("jacontent....." + jacontent);
		ArrayList<String> contentList = new ArrayList<String>();
		DocumentParsing dp = new DocumentParsing();
		contentList = dp.ParsingAllContent(jacontent);

		String engcontent = "";
		String myancontent = "";

		int i = 0;
		for (String content : contentList) {
			if (i % 2 == 0) {
				engcontent = engcontent.concat(content);
			} else {
				myancontent = myancontent.concat(content);
			}
			i++;
		}

		int start = engcontent.indexOf("<audio>") + 7;
		int end = engcontent.indexOf("</audio>");
		String resultString = "";
		if (start > 0 && end > 0) {
			String str = engcontent.substring(start, end);
			resultString = "<audio controls><source src=" + str + " type=\"audio/mpeg\">";
			// newJournal.setAudioLink(str);
			newJournal.setEngContent(resultString + engcontent.substring(end, engcontent.length()));
			newJournal.setMyanmarContent(resultString + myancontent.substring(end, myancontent.length()));
			newJournal.setIosEngContent(Jsoup.parse(engcontent.substring(end, engcontent.length())).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent.substring(end, myancontent.length())).text());
		} else {
			newJournal.setEngContent(engcontent);
			newJournal.setMyanmarContent(myancontent);
			newJournal.setIosEngContent(Jsoup.parse(engcontent).text());
			newJournal.setIosMyaContent(Jsoup.parse(myancontent).text());
		}
		// System.out.println("myancontent....."+jacontent);
		List<String> myanmarList = getNameList("Name", jacontent, "my_MM");
		if (!CollectionUtils.isEmpty(myanmarList)) {
			newJournal.setmNameList(myanmarList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : myanmarList)
				hashlist.put(index++, list);
			newJournal.setiOSmNameList(hashlist);
		}

		List<String> engList = getNameList("Name", jacontent, "en_US");
		if (!CollectionUtils.isEmpty(engList)) {
			newJournal.seteNameList(engList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : engList)
				hashlist.put(index++, list);
			newJournal.setiOSeNameList(hashlist);
		}

		List<String> myanmarLinkList = getNameList("Link", jacontent, "my_MM");
		if (!CollectionUtils.isEmpty(myanmarLinkList)) {
			newJournal.setmLinkList(myanmarLinkList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : myanmarLinkList)
				hashlist.put(index++, list);
			newJournal.setiOSmLinkList(hashlist);
		}

		List<String> engLinkList = getNameList("Link", jacontent, "en_US");
		if (!CollectionUtils.isEmpty(engLinkList)) {
			newJournal.seteLinkList(engLinkList);
			Integer index = 1;
			HashMap<Integer, String> hashlist = new HashMap<Integer, String>();
			for (String list : engLinkList)
				hashlist.put(index++, list);
			newJournal.setiOSeLinkList(hashlist);
		}
		return newJournal;
	}

	private JSONObject getAssetCategoryByVocabularyId(long vocabularId, AboutMyanmarType type) {
		JSONObject resultJson = new JSONObject();
		List<AssetCategory> catList = assetCategoryService.getAssetCategoryNameExceptionByVocalbularyId(vocabularId);
		List<AssetCategory> categories = getAssetCategories(catList);
		resultJson.put("aboutmyanmar", categories);
		return resultJson;
	}

	private List<AssetCategory> getAssetCategories(List<AssetCategory> entryList) {
		List<AssetCategory> categoryList = new ArrayList<AssetCategory>();
		for (AssetCategory category : entryList) {
			List<AssetCategoryProperty> properties = assetCategoryPropertyService.getAssetCategoryPropertyByCategoryId(category.getCategoryid());

			String detailUrl = properties.get(0).getValue();
			String imageUrl = properties.get(1).getValue();
			DocumentParsing dp = new DocumentParsing();
			String title[] = dp.ParsingTitle(category.getTitle());
			category.setEngtitle(title[0]);
			category.setMyantitle(title[1]);
			JournalArticle journal = new JournalArticle();
			Base32 base32 = new Base32();

			byte[] imagedecodedBytes = base32.decode(imageUrl);
			String imagedecodedString = new String(imagedecodedBytes);
			byte[] detaildecodedBytes = base32.decode(detailUrl);
			String detaildecodedString = new String(detaildecodedBytes);
			category.setImageurl("https://myanmar.gov.mm" + imagedecodedString);
			category.setDetailurl("https://myanmar.gov.mm" + detaildecodedString);
			AssetEntry ae = new AssetEntry();
			if (category.getCategoryid() != 80317) {
				if (category.getCategoryid() == 80297) {
					ae = assetEntryService.getAssetEntryByClassTypeCategoryIdTitle(category.getCategoryid(), 5363176, category.getMyantitle());

				} else {
					ae = assetEntryService.getAssetEntryByClassTypeCategoryIdTitle(category.getCategoryid(), 36196, category.getMyantitle());
				}

				journal = journalArticleService.byClassPK(ae.getClasspk());

				List<String> engimage = dp.ParsingEngImage(journal.getContent());
				List<String> myanimage = dp.ParsingMyanImage(journal.getContent());
				String engimages = "";
				String myanimages = "";
				for (String eng : engimage)
					engimages += eng;
				for (String myan : myanimage)
					myanimages += myan;

				category.setEngImageUrl(engimages);
				category.setMyanImageUrl(myanimages);
				ArrayList<String> contentList = new ArrayList<String>();
				contentList = dp.ParsingAllContent(journal.getContent());

				String engcontent = "";
				String myancontent = "";

				int i = 0;
				for (String content : contentList) {
					if (i % 2 == 0) {
						engcontent = engcontent.concat(content);
					} else {
						myancontent = myancontent.concat(content);
					}
					i++;
				}

				if (category.getCategoryid() == 80297) {
					int start = engcontent.indexOf("<audio>") + 7;
					int end = engcontent.indexOf("</audio>");
					String str = engcontent.substring(start, end);
					String resultString = "<audio controls><source src=" + str + " type=\"audio/mpeg\">";
					resultString += "<source src=" + str + " type=\"audio/ogg\">";
					resultString += "<source src=" + str + " type=\"audio/wav\">";
					// System.out.println("audio control...."+resultString);
					category.setAudioLink(str);
					category.setEngcontent(resultString + engcontent.substring(end, engcontent.length()).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setMyancontent(resultString + myancontent.substring(end, myancontent.length()).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setIosEngContent(Jsoup.parse(category.getEngcontent()).text());
					category.setIosMyaContent(Jsoup.parse(category.getMyancontent()).text());

				} else {

					String[] engmyan = dp.ParsingContent(journal.getContent());
					String engContent = engmyan[0].replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "");
					String myaContent = engmyan[1].replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", "");
					String iosEngContent = Jsoup.parse(engContent).text();
					String iosMyaContent = Jsoup.parse(myaContent).text();

					category.setIosEngContent(iosEngContent);
					category.setIosMyaContent(iosMyaContent);
					category.setEngcontent(engContent);
					category.setMyancontent(myaContent);
				}
			}

			categoryList.add(category);
		}
		return categoryList;
	}

	public List<JournalArticle> getJournalArticles(List<String> classUuids) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		for (String classUuid : classUuids) {
			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(classUuid);
			if (journalArticle != null)
				journalArticleList.add(journalArticle);
		}
		return journalArticleList;
	}

	private JSONObject getJournalArticlesTravel(AboutMyanmarType type) {
		JSONObject resultJson = new JSONObject();
		AssetEntry travel = assetEntryService.getAssetEntryByClassTypeCategoryIdTitle(80317, 36196, "ခရီးသွားလမ်းညွှန်");
		AssetEntry visa = assetEntryService.getAssetEntryByClassType(36199);
		AssetEntry embassy = assetEntryService.getAssetEntryByClassType(99099);

		JournalArticle travelja = parseJournalArticle(journalArticleService.byClassPK(travel.getClasspk()));
		JournalArticle visaja = parseJournalArticleforVisa(journalArticleService.byClassPK(visa.getClasspk()));
		JournalArticle embassyja = parseJournalArticleforEmbassy(journalArticleService.byClassPK(embassy.getClasspk()));
		List<Long> destination = assetEntryService.getClassPkList(36187);
		List<JournalArticle> destinationList = new ArrayList<JournalArticle>();
		for (Long classPk : destination) {
			JournalArticle journal = parseJournalArticleforDestination(journalArticleService.byClassPK(classPk));
			if (journal != null)
				destinationList.add(journal);
		}

		resultJson.put("travel", travelja);
		resultJson.put("visa", visaja);
		resultJson.put("embassy", embassyja);
		resultJson.put("destinations", destinationList);
		return resultJson;
	}

	@RequestMapping(value = "home", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getAboutMyanmar(@RequestHeader("Authorization") String encryptedString) {
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
		// 80289
		return getAssetCategoryByVocabularyId(80289, AboutMyanmarType.ABOUT);
	}

	@RequestMapping(value = "travel", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getTravel(@RequestHeader("Authorization") String encryptedString) {
		// 36017
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
		return getJournalArticlesTravel(AboutMyanmarType.TRAVEL);
	}

}
