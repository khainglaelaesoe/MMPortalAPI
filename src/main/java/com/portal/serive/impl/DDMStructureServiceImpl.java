package com.portal.serive.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.portal.dao.DDMStructureDao;
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
}
