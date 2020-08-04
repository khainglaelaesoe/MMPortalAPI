package com.portal.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.CategoryType;
import com.portal.entity.DateUtil;
import com.portal.entity.JournalArticle;
import com.portal.entity.Views;
import com.portal.service.AssetEntryService;
import com.portal.service.DDLRecordService;
import com.portal.service.DDMStructureService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("survey")
public class SurveyController extends AbstractController {

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private DDLRecordService ddlRecordService;

	@Autowired
	private DDMStructureService ddmStructureService;

	private static Logger logger = Logger.getLogger(SurveyController.class);
	private static JSONParser jsonParser = new JSONParser();

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {

		String categoryType = getEngElement(journalArticle.getContent(), "Type", "<dynamic-content language-id=\"en_US\">").isEmpty() ? getEngElement(journalArticle.getContent(), "Type", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Type", "<dynamic-content language-id=\"en_US\">");
		CategoryType type = CategoryType.valueOf(categoryType.trim().toUpperCase());
		if (type != CategoryType.SURVEY)
			return null;

		/* title, department title, content detail */
		JournalArticle newJournal = new JournalArticle();
		Document doc = Jsoup.parse(journalArticle.getTitle());
		replaceTag(doc.children());
		List<String> titleList = removeInvalidString(Jsoup.parse(doc.toString()).text().split("/"));
		newJournal.setEngTitle(titleList.get(0));
		newJournal.setMynamrTitle(titleList.size() > 1 ? titleList.get(1) : titleList.get(0));
		newJournal.setEngDepartmentTitle(getEngElement(journalArticle.getContent(), "Department", "<dynamic-content language-id=\"en_US\">"));
		newJournal.setMyanmarDepartmentTitle(getMyanmarElement(journalArticle.getContent(), "Department", "<dynamic-content language-id=\"my_MM\">"));
		String dateString = journalArticle.getDisplaydate().split(" ")[0];
		String[] dateStr = dateString.split("-");
		String resultDateString = DateUtil.getCalendarMonthName(Integer.parseInt(dateStr[1]) - 1) + " " + dateStr[2] + " " + dateStr[0];
		newJournal.setDisplaydate(resultDateString);
		String pollOrSurveyId = getEngElement(journalArticle.getContent(), "PollOrSurveyId", "<dynamic-content language-id=\"en_US\">").isEmpty() ? getEngElement(journalArticle.getContent(), "PollOrSurveyId", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "PollOrSurveyId", "<dynamic-content language-id=\"en_US\">");
		long count = ddlRecordService.getCountOfVoteOrSurvey(Long.parseLong(pollOrSurveyId));

		String definition = ddmStructureService.getDefinition(Long.parseLong(pollOrSurveyId));
		int start = definition.indexOf("option");
		String str = definition.substring(start, definition.length());
		List<String> engQues = new ArrayList<String>();
		List<String> myanmarQues = new ArrayList<String>();
		String newString = str.replaceAll(",\"", "").replaceAll("}", "").replaceAll("\"", "").replaceAll(":", "").replaceAll("label", "").replaceAll("options", "").replaceAll(",", "").replaceAll("my_MMvalidation", "").replaceAll("localizabletruetip", "").replaceAll("]", "").replaceAll("value", "@").replaceAll("en_US", "@").replaceAll("my_MM", "@");
		String[] arr = newString.split("@");

		if (arr.length > 0) {
			if (str.contains("en_US")) {
				engQues.add(arr[1]);
				engQues.add(arr[4]);
				engQues.add(arr[7]);
				engQues.add(arr[10]);
				myanmarQues.add(arr[2]);
				myanmarQues.add(arr[5]);
				myanmarQues.add(arr[8]);
				myanmarQues.add(arr[11]);
			} else {
				myanmarQues.add(arr[1]);
				myanmarQues.add(arr[3]);
				myanmarQues.add(arr[5]);
				myanmarQues.add(arr[7]);
			}
		}

		newJournal.setMyanmarQuestions(myanmarQues);
		newJournal.setEngQuestions(engQues);
		newJournal.setPollOrSurveyCount(count);
		newJournal.setShareLink(getShareLink(pollOrSurveyId));
		return newJournal;
	}

	private String getShareLink(String questionId) {
		return "https://myanmar.gov.mm/my/survey?p_p_id=com_liferay_dynamic_data_lists_web_portlet_DDLDisplayPortlet_INSTANCE_4psheAaJMxqG&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&_com_liferay_dynamic_data_lists_web_portlet_DDLDisplayPortlet_INSTANCE_4psheAaJMxqG_mvcPath=%2Fedit_record.jsp&_com_liferay_dynamic_data_lists_web_portlet_DDLDisplayPortlet_INSTANCE_4psheAaJMxqG_recordSetId=" + questionId;
	}

	private Map get(String definition) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		int start = definition.indexOf("en_US") + 8;
		int end = definition.indexOf("\",");

		if (start < 0 || end < 0)
			return map;

		map.put(end, definition.substring(start, end));
		return map;
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

	private List<JournalArticle> parseJournalArticleList(List<JournalArticle> journalArticleList) {
		List<JournalArticle> newJournalList = new ArrayList<JournalArticle>();
		for (JournalArticle journalArticle : journalArticleList) {
			JournalArticle journal = parseJournalArticle(journalArticle);
			if (journal != null)
				newJournalList.add(journal);
		}

		return newJournalList;
	}

	public List<JournalArticle> getJournalArticles(List<String> entryList) {
		List<JournalArticle> journalArticleList = new ArrayList<JournalArticle>();
		for (String classUuid : entryList) {
			JournalArticle journalArticle = journalArticleService.getJournalArticleByAssteEntryClassUuId(classUuid);
			if (journalArticle != null)
				journalArticleList.add(journalArticle);
		}
		return journalArticleList;
	}

	@RequestMapping(value = "all", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getSurvey(@RequestParam("input") String input, @RequestParam("userid") String userid) {
		JSONObject resultJson = new JSONObject();
		List<String> entryList = assetEntryService.getClassuuidListForPollAndSurvey(104266);
		entryList.addAll(assetEntryService.getClassuuidListForPollAndSurvey(104253));
		List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList));
		int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("survey", byPaganation(journalArticleList, input));
		resultJson.put("totalCount", journalArticleList.size());
		return resultJson;
	}

	@RequestMapping(value = "bysearchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getSurerys(@RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("userid") String userid) {
		JSONObject resultJson = new JSONObject();
		List<JournalArticle> resultList = new ArrayList<JournalArticle>();
		List<String> entryList = assetEntryService.getClassuuidListForPollAndSurvey(104266);
		entryList.addAll(assetEntryService.getClassuuidListForPollAndSurvey(104253));
		List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList));
		for (JournalArticle journalArticle : journalArticleList) {
			StringBuilder searchterms = new StringBuilder();
			searchterms.append(journalArticle.getMyanmarDepartmentTitle());
			searchterms.append(journalArticle.getEngDepartmentTitle());
			searchterms.append(journalArticle.getMynamrTitle());
			searchterms.append(journalArticle.getEngTitle());
			searchterms.append(journalArticle.getDisplaydate());
			if (searchterms.toString().contains(searchterm))
				resultList.add(journalArticle);
		}

		int lastPageNo = resultList.size() % 10 == 0 ? resultList.size() / 10 : resultList.size() / 10 + 1;
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("survey", byPaganation(resultList, input));
		resultJson.put("totalCount", resultList.size());
		return resultJson;
	}
}
