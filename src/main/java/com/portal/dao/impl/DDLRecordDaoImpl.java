package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.DDLRecordDao;
import com.portal.entity.DDLRecord;

@Repository
public class DDLRecordDaoImpl extends AbstractDaoImpl<DDLRecord, String> implements DDLRecordDao {

	protected DDLRecordDaoImpl() {
		super(DDLRecord.class);
	}

}
