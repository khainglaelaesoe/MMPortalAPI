package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.DDMStructureDao;
import com.portal.entity.DDMStructure;

@Repository
public class DDMStructureDaoImpl extends AbstractDaoImpl<DDMStructure, String> implements DDMStructureDao {

	protected DDMStructureDaoImpl() {
		super(DDMStructure.class);
	}
}
