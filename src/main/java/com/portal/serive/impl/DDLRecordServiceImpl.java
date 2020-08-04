package com.portal.serive.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.DDLRecordDao;
import com.portal.service.DDLRecordService;

@Service("ddlRecordService")
public class DDLRecordServiceImpl implements DDLRecordService {
	
	@Autowired
	private DDLRecordDao ddlRecordDao;

	public long getCountOfVoteOrSurvey(long pollOrSurveyId) {
		String query = "Select count(*) from DDLRecord where recordsetid=" + pollOrSurveyId;
		return ddlRecordDao.findLongByQueryString(query).get(0);
	}
}
