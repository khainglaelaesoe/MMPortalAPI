package com.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.JournalArticle;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.AssetEntryService;
import com.portal.service.JournalArticleService;

@Controller
@RequestMapping("breakingNews")
public class BreakingNewController extends AbstractController {
	@Autowired
	private JournalArticleService journalArticleService;

	@Autowired
	private AssetEntryService assetEntryService;

	@RequestMapping(value = "getBreakingNews", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getBreakingNews() {
		JSONObject resultJson = new JSONObject();
		long classPK = assetEntryService.getClassPK();
		JournalArticle journal = journalArticleService.getJournalArticleByClassPK(classPK);
		List<Map<String, String>> contentlist = new DocumentParsing().ParsingImageTextTextArea(journal.getContent());
		String title[] = new DocumentParsing().ParsingTitle(journal.getTitle());
		String englishTitle = title[0];
		String myanmarTitle = title[1];
		resultJson.put("englishTitle", englishTitle.isEmpty() ? myanmarTitle : englishTitle);
		resultJson.put("myanmarTitle", myanmarTitle.isEmpty() ? englishTitle : myanmarTitle);
		resultJson.put("contentList", contentlist);
		return resultJson;
	}
}
