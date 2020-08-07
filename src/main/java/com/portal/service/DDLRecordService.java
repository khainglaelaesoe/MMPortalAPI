package com.portal.service;

public interface DDLRecordService {

	public long getCountOfVoteOrSurvey(long pollOrSurveyId);
	public boolean getCountOfSurveybyuserid(long pollOrSurveyId, long userid);

}
