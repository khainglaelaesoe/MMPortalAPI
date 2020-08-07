package com.portal.serive.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.controller.PollController;
import com.portal.dao.PollsChoiceDao;
import com.portal.entity.PollsChoice;
import com.portal.service.PollsChoiceService;

@Service("pollsChoiceService")
public class PollsChoiceServiceImpl implements PollsChoiceService {

	@Autowired
	private PollsChoiceDao pollsChoiceDao;

	public List<String> getDescription(long pollOrSurveyId) {
		String query = "Select description from PollsChoice where choiceid in (select choiceid from PollsVote where questionid=" + pollOrSurveyId + ")";
		return pollsChoiceDao.findByQuery(query);
	}
	public List<PollsChoice> getDescription1(long pollOrSurveyId) {
		String query = "Select pollschoice from PollsChoice pollschoice where choiceid in (select choiceid from PollsVote where questionid=" + pollOrSurveyId + ")";
		return pollsChoiceDao.byQuery(query);
	}
	
	public List<PollsChoice> getVoltResult(long pollOrSurveyId) {
		String query = "select pollschoice from PollsChoice pollschoice where questionid=" + pollOrSurveyId;
		List<PollsChoice> pollsList = pollsChoiceDao.byQuery(query);
		return get(pollsList,pollOrSurveyId);
	}
	
	public List<PollsChoice> get(List<PollsChoice> pollsList,long pollOrSurveyId){
		 List<PollsChoice> pollsListRes = new ArrayList<PollsChoice>();
		long pollchoicecount = 0;
		for(PollsChoice pollschoice : pollsList) {
			String query ="select count(*) from PollsVote where choiceid =" + pollschoice.getChoiceid();
			pollchoicecount = pollsChoiceDao.findLongByQueryString(query).get(0);
			pollschoice.setChoicecount(pollchoicecount);
			pollsListRes.add(pollschoice);
		}
		return pollsList;
	}
	
	public long getCountOfVote(long pollOrSurveyId) {
		String query = "Select count(*) from PollsVote where questionid=" + pollOrSurveyId;
		return pollsChoiceDao.findLongByQueryString(query).get(0);
	}
}
