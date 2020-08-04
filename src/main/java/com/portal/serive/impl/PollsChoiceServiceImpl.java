package com.portal.serive.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.PollsChoiceDao;
import com.portal.service.PollsChoiceService;

@Service("pollsChoiceService")
public class PollsChoiceServiceImpl implements PollsChoiceService {

	@Autowired
	private PollsChoiceDao pollsChoiceDao;

	public List<String> getDescription(long pollOrSurveyId) {
		String query = "Select description from PollsChoice where choiceid in (select choiceid from PollsVote where questionid=" + pollOrSurveyId + ")";
		return pollsChoiceDao.findByQuery(query);
	}
}
