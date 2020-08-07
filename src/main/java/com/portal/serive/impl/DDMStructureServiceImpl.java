package com.portal.serive.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.portal.dao.DDMStructureDao;
import com.portal.entity.DDMStructure;
import com.portal.entity.PollsChoice;
import com.portal.service.DDMStructureService;

@Service("ddmStructureService")
public class DDMStructureServiceImpl implements DDMStructureService {

	@Autowired
	private DDMStructureDao ddmStructureDao;

	private static Logger logger = Logger.getLogger(DDMStructureServiceImpl.class);

	public String getDefinition(Long pollOrSurveyId) {
		String query = "Select definition from DDMStructure ddm where structureid in (Select ddmStructureid from DDLRecordSet ddl where recordsetid=" + pollOrSurveyId + ")";
		List<String> definitionList = ddmStructureDao.findByQuery(query);
		if (!CollectionUtils.isEmpty(definitionList))
			return definitionList.get(0);
		return "";
	}
	
	public List<DDMStructure> getSurveyResult(long pollOrSurveyId) {
		String query = "Select ddm from DDMStructure ddm where structureid in (select ddmStructureid from DDLRecordSet where recordsetid=" + pollOrSurveyId + ")";
		List<DDMStructure> pollsList = ddmStructureDao.byQuery(query);
		return get(pollsList,pollOrSurveyId);
	}
	
	public List<DDMStructure> get(List<DDMStructure> ddmsList,long pollOrSurveyId){
		long pollchoicecount = 0;
		for(DDMStructure ddmstructure : ddmsList) {
			String query ="select count(*) from DDLRecordSet where ddmStructureid =" + ddmstructure.getStructureid();
			pollchoicecount = ddmStructureDao.findLongByQueryString(query).get(0);
			ddmstructure.setChoicecount(pollchoicecount);
		}
		return ddmsList;
	}
	
	public long getCountOfSurvey(long pollOrSurveyId) {
		String query = "Select count(*) from DDLRecordSet where ddmStructureid=" + pollOrSurveyId;
		return ddmStructureDao.findLongByQueryString(query).get(0);
	}
	
	public boolean getCountOfSurveybyuserid(long pollOrSurveyId, long userid) {
		String query = "Select count(*) from DDLRecordSet where ddmStructureid=" + pollOrSurveyId + " and userid=" + userid;
		if(ddmStructureDao.findLong(query) > 0)
			return true;
		else
			return false;
	}
}
