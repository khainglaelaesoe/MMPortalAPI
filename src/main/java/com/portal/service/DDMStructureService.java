package com.portal.service;

import java.util.List;

import com.portal.entity.DDMStructure;
import com.portal.entity.PollsChoice;

public interface DDMStructureService {

	public String getDefinition(Long pollOrSurveyId);
	
	public List<DDMStructure> getSurveyResult(long pollOrSurveyId);
	
	public long getCountOfSurvey(long pollOrSurveyId);
	
	public boolean getCountOfSurveybyuserid(long pollOrSurveyId, long userid);

}
