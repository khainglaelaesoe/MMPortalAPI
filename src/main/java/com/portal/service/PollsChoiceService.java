package com.portal.service;

import java.util.List;

import com.portal.entity.PollsChoice;

public interface PollsChoiceService {

	public List<PollsChoice> getDescription(long pollOrSurveyId);
	public List<PollsChoice> getVoltResult(long pollOrSurveyId);
	public long getCountOfVote(long pollOrSurveyId);

}
