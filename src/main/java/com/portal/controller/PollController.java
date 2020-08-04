package com.portal.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
import com.portal.service.JournalArticleService;
import com.portal.service.PollsChoiceService;
import com.portal.service.PollsVoteService;

@Controller
@RequestMapping("poll")
public class PollController extends AbstractController {

	@Autowired
	private AssetEntryService assetEntryService;

	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private PollsVoteService pollsVoteService;

	@Autowired
	private PollsChoiceService pollsChoiceService;

	private static Logger logger = Logger.getLogger(PollController.class);

	private JournalArticle parseJournalArticle(JournalArticle journalArticle) {
		if (journalArticle.getContent() == null)
			return null;

		String categoryType = getEngElement(journalArticle.getContent(), "Type", "<dynamic-content language-id=\"en_US\">").isEmpty() ? getEngElement(journalArticle.getContent(), "Type", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "Type", "<dynamic-content language-id=\"en_US\">");
		CategoryType type = CategoryType.valueOf(categoryType.trim().toUpperCase());
		if (type != CategoryType.VOTE)
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

		String pollOrSurveyId = getEngElement(journalArticle.getContent(), "PollOrSurveyId", "<dynamic-content language-id=\"en_US\">").isEmpty() ? getMyanmarElement(journalArticle.getContent(), "PollOrSurveyId", "<dynamic-content language-id=\"my_MM\">") : getEngElement(journalArticle.getContent(), "PollOrSurveyId", "<dynamic-content language-id=\"en_US\">");
		long count = pollsVoteService.getCountOfVote(Long.parseLong(pollOrSurveyId));
		newJournal.setPollOrSurveyCount(count);

		List<String> engQues = new ArrayList<String>();
		List<String> myanmarQues = new ArrayList<String>();
		List<String> descriptions = pollsChoiceService.getDescription(Long.parseLong(pollOrSurveyId));
		for (String description : descriptions) {
			Document doc2 = Jsoup.parse(description, "", Parser.xmlParser());
			Elements element = doc2.getElementsByTag("Description");
			engQues.add(Jsoup.parse(element.get(0).toString()).text());
			myanmarQues.add(Jsoup.parse(element.get(1).toString()).text());
		}

		newJournal.setShareLink(getShareLink(pollOrSurveyId));
		newJournal.setEngQuestions(engQues);
		newJournal.setMyanmarQuestions(myanmarQues);
		return newJournal;
	}

	private String getShareLink(String questionId) {
		return "https://myanmar.gov.mm/poll?p_p_id=com_liferay_polls_web_portlet_PollsDisplayPortlet_INSTANCE_Mr4wSm0Fo13J&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&_com_liferay_polls_web_portlet_PollsDisplayPortlet_INSTANCE_Mr4wSm0Fo13J_mvcPath=%2Fpolls_display%2Fview.jsp&_com_liferay_polls_web_portlet_PollsDisplayPortlet_INSTANCE_Mr4wSm0Fo13J_pollsQuestionId=" + questionId;
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
	public JSONObject getPolls(@RequestParam("input") String input, @RequestParam("userid") String userid) {
		JSONObject resultJson = new JSONObject();
		List<String> entryList = assetEntryService.getClassuuidListForPollAndSurvey(104266);
		entryList.addAll(assetEntryService.getClassuuidListForPollAndSurvey(104253));
		List<JournalArticle> journalArticleList = parseJournalArticleList(getJournalArticles(entryList));
		int lastPageNo = journalArticleList.size() % 10 == 0 ? journalArticleList.size() / 10 : journalArticleList.size() / 10 + 1;
		resultJson.put("lastPageNo", lastPageNo);
		resultJson.put("poll", byPaganation(journalArticleList, input));
		resultJson.put("totalCount", journalArticleList.size());
		return resultJson;
	}

	@RequestMapping(value = "bysearchterm", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getPolls(@RequestParam("searchterm") String searchterm, @RequestParam("input") String input, @RequestParam("userid") String userid) {
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
		resultJson.put("poll", byPaganation(resultList, input));
		resultJson.put("totalCount", resultList.size());
		return resultJson;
	}
}
