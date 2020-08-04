package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.JournalFolderDao;
import com.portal.entity.JournalFolder;

@Repository
public class JournalFolderDaoImpl extends AbstractDaoImpl<JournalFolder, String> implements JournalFolderDao {

	protected JournalFolderDaoImpl() {
		super(JournalFolder.class);
	}
}
