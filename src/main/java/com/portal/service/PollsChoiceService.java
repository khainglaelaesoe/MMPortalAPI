package com.portal.service;

import java.util.List;

import com.portal.entity.PollsChoice;

public interface PollsChoiceService {
	
	public List<String> getDescription(long pollOrSurveyId);
	public List<PollsChoice> getDescription1(long pollOrSurveyId);
	public List<PollsChoice> getVoltResult(long pollOrSurveyId);
	public long getCountOfVote(long pollOrSurveyId);

}
