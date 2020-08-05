package com.portal.service;

public interface PollsVoteService {

	public long getCountOfVote(long pollOrSurveyId);
	public boolean getCountOfVotebyuserid(long pollOrSurveyId, long userid);

}
