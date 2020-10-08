package com.portal.serive.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.PollsVoteDao;
import com.portal.service.PollsVoteService;

@Service("pollsVoteService")
public class PollsVoteServiceImpl implements PollsVoteService {

	@Autowired
	private PollsVoteDao pollsVoteDao;

	public long getCountOfVote(long pollOrSurveyId) {
		String query = "Select count(*) from PollsVote where questionid=" + pollOrSurveyId;
		return pollsVoteDao.findLongByQueryString(query).get(0);
	}
	
	public boolean getCountOfVotebyuserid(long pollOrSurveyId, long userid) {
		String query = "Select count(*) from PollsVote where questionid=" + pollOrSurveyId + " and userid=" + userid;
		if(pollsVoteDao.findLong(query) > 0)
			return true;
		else
			return false;
	}
}
