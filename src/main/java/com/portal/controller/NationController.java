package com.portal.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base32;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.JournalArticle;
import com.portal.entity.NationType;
import com.portal.entity.Views;
import com.portal.entity.AES;
import com.portal.entity.AssetCategory;
import com.portal.entity.AssetCategoryProperty;
import com.portal.entity.AssetEntry;
import com.portal.parsing.DocumentParsing;
import com.portal.entity.Nation;
import com.portal.service.AssetCategoryPropertyService;
import com.portal.service.AssetCategoryService;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("nation")
public class NationController extends AbstractController {

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private AssetCategoryService assetCategoryService;

	@Autowired
	private AssetCategoryPropertyService assetCategoryPropertyService;

	private static Logger logger = Logger.getLogger(JournalArticleController.class);

	private List<String> getLink(int index, String content, String remover) {
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

	private JSONObject getAssetCategoryByParentCategoryId(long pcategoryId) {
		JSONObject resultJson = new JSONObject();

		/*
		 * Nation.stream().filter(d ->
		 * d.getCategoryId()==pcategoryId).forEach(System.out::println);
		 */

		logger.info("pcategoryId !!!!!!!!!" + pcategoryId);

		List<Nation> enumNames = Nation.stream().filter(d -> d.getCategoryId() == pcategoryId).collect(Collectors.toList());

		List<AssetCategory> catList = new ArrayList<AssetCategory>();

		for (Nation nation : enumNames) {
			logger.info("nation.getMyanName() !!!!!!!!!" + nation.getMyanName());

			AssetCategory category = assetCategoryService.getAssetCategoryByParentCategoryIdandName(pcategoryId, nation.getMyanName().trim());
			if (category != null) {
				logger.info("category !!!!!!!" + category);
				catList.add(category);
			}
		}

		// List<AssetCategory> catList =
		// assetCategoryService.getAssetCategoryByParentCategoryId(pcategoryId);

		List<AssetCategory> categories = parseAssetCategories(catList);
		resultJson.put("nation", categories);
		return resultJson;
	}

	/* ministry */
	private JSONObject getAssetCategoryByParentCategoryMinistry(long pcategoryId, NationType type) {
		JSONObject resultJson = new JSONObject();

		List<AssetCategory> catList = new ArrayList<AssetCategory>();
		System.out.println("list size.........." + catList);

		List<Nation> enumNames = Nation.stream().filter(d -> d.getCategoryId() == pcategoryId).collect(Collectors.toList());

		for (Nation nation : enumNames) {
			AssetCategory category = assetCategoryService.getAssetCategoryByParentCategoryIdandName(pcategoryId, nation.getMyanName());
			if (category != null)
				catList.add(category);
		}

		List<AssetCategory> categories = parseAssetCategories(catList);
		resultJson.put("nation", categories);
		return resultJson;
	}

	/* ministry */
	private List<AssetCategory> parseAssetCategories(List<AssetCategory> idList) {
		List<AssetCategory> categoryList = new ArrayList<AssetCategory>();
		for (AssetCategory category : idList) {
			List<AssetCategoryProperty> properties = assetCategoryPropertyService.getAssetCategoryPropertyByCategoryId(category.getCategoryid());
			String detailUrl = "";
			String imageUrl = "";
			for (int i = 0; i < properties.size(); i++) {
				if (properties.get(i).getKey_().equals("image"))
					imageUrl = properties.get(i).getValue();
				if (properties.get(i).getKey_().equals("link"))
					detailUrl = properties.get(i).getValue();
			}

			DocumentParsing dp = new DocumentParsing();
			String title[] = dp.ParsingTitle(category.getTitle());
			category.setEngtitle(title[0]);
			category.setMyantitle(title[1]);
			Base32 base32 = new Base32();

			byte[] imagedecodedBytes = base32.decode(imageUrl);
			String imagedecodedString = new String(imagedecodedBytes);
			byte[] detaildecodedBytes = base32.decode(detailUrl);
			String detaildecodedString = new String(detaildecodedBytes);
			category.setImageurl("https://myanmar.gov.mm" + imagedecodedString);
			category.setDetailurl("https://myanmar.gov.mm" + detaildecodedString);
			JournalArticle ja = new JournalArticle();
			if (category.getCategoryid() != 8251623 && category.getCategoryid() != 87166 && category.getCategoryid() != 8249564 && category.getCategoryid() != 87195) {
				AssetEntry ae = new AssetEntry();

				ae = assetEntryService.getAssetEntryByClassTypeCategoryTitle(category.getCategoryid(), category.getEngtitle(), category.getMyantitle());
				ja = journalArticleService.byClassPK(ae.getClasspk());

				ArrayList<String> contentList = new ArrayList<String>();
				String language = dp.AvailableLanguage(ja.getContent());
				category.setLanguage(language);
				if (category.getLanguage() != "en_US,my_MM") {
					String imagepath = dp.ParsingImage(ja.getContent());
					if (imagepath != null)
						category.setArticleImage(imagepath);
				}
				contentList = dp.ParsingAllContent(ja.getContent());

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

				if (language.equals("en_US")) {
					if (engcontent.equals(""))
						engcontent = myancontent;
					myancontent = engcontent;
				}
				if (language.equals("my_MM")) {
					if (myancontent.equals(""))
						myancontent = engcontent;
					engcontent = myancontent;
				}

				engcontent = dp.ParsingSpan(engcontent);
				myancontent = dp.ParsingSpan(myancontent);

				if (myancontent.contains("data:image/png;base64")) {
					Document doc = Jsoup.parse(myancontent, "", Parser.htmlParser());
					Elements imgs = doc.select("img[src^=data:image/png;base64]");
					String base64String = "";
					for (Element element : imgs) {
						String attr = element.attr("src");
						base64String = attr.substring(22);
					}
					category.setBase64image(base64String);
				}

				int start = engcontent.indexOf("<image>") + 7;
				int end = engcontent.indexOf("</image>");
				String resultString = "";
				if (start > 0 && end > 0) {
					String str = engcontent.substring(start, end);
					resultString = "<img src=" + str + "\">";

					category.setAudioLink(str);
					category.setEngcontent(resultString + engcontent.substring(end + 8, engcontent.length()).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setMyancontent(resultString + myancontent.substring(end + 8, myancontent.length()).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setIosEngContent(Jsoup.parse(engcontent.substring(end + 8, engcontent.length())).text());
					category.setIosMyaContent(Jsoup.parse(myancontent.substring(end + 8, myancontent.length())).text());
				} else {
					category.setEngcontent(engcontent.replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setMyancontent(myancontent.replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setEngcontent(resultString + engcontent.substring(end + 8, engcontent.length()).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));

					category.setMyancontent(resultString + myancontent.substring(end + 8, myancontent.length()).replaceAll("<html>", "").replaceAll("</html>", "").replaceAll("<head>", "").replaceAll("</head>", "").replaceAll("<body>", "").replaceAll("</body>", "").replaceAll("\n \n \n", ""));
					category.setIosEngContent(Jsoup.parse(engcontent.substring(end + 8, engcontent.length())).text());
					category.setIosMyaContent(Jsoup.parse(myancontent.substring(end + 8, myancontent.length())).text());
				}
			} // for
			categoryList.add(category);
		} // if

		return categoryList;
	}

	@RequestMapping(value = "home", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getNation(@RequestHeader("Authorization") String encryptedString) {
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
		// 80624
		return getAssetCategoryByParentCategoryId(80624);
	}

	@RequestMapping(value = "hluttaw", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getHluttaw(@RequestHeader("Authorization") String encryptedString) {
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
		// 8248752
		return getAssetCategoryByParentCategoryId(8248752);
	}

	@RequestMapping(value = "government", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getGov(@RequestHeader("Authorization") String encryptedString) {
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
		// 80624
		return getAssetCategoryByParentCategoryId(80624);
	}

	@RequestMapping(value = "uniongovernment", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getUnionGov(@RequestHeader("Authorization") String encryptedString) {
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
		// 80624
		return getAssetCategoryByParentCategoryId(8251623);
	}

	@RequestMapping(value = "judices", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getJudices(@RequestHeader("Authorization") String encryptedString) {
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
		// 80625
		return getAssetCategoryByParentCategoryId(80625);
	}

	@RequestMapping(value = "commission", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getCommission(@RequestHeader("Authorization") String encryptedString) {
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
		// 80626
		return getAssetCategoryByParentCategoryId(80626);
	}

	@RequestMapping(value = "ngo", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getNgo(@RequestHeader("Authorization") String encryptedString) {
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
		// 9590639
		return getAssetCategoryByParentCategoryId(9590639);
	}

	@RequestMapping(value = "srgovernment", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getSRGovernment(@RequestHeader("Authorization") String encryptedString) {
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
		// 9590639
		return getAssetCategoryByParentCategoryId(87195);
	}

	@RequestMapping(value = "srhluttaw", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getSRHluttaw(@RequestHeader("Authorization") String encryptedString) { // 8249564
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
		return getAssetCategoryByParentCategoryId(8249564);
	}

	@RequestMapping(value = "ministry", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getMinistry(@RequestHeader("Authorization") String encryptedString) {
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
		// 9590639
		return getAssetCategoryByParentCategoryMinistry(87166, NationType.MINISTRY);
		// return getAssetCategoryByParentCategoryId(87166);
	}

}
