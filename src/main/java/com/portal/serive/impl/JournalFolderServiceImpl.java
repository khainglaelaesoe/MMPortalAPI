package com.portal.serive.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.JournalFolderDao;
import com.portal.service.JournalFolderService;

@Service("journalFolderService")
public class JournalFolderServiceImpl implements JournalFolderService {

	@Autowired
	private JournalFolderDao journalFolderDao;

	public String getNameByFolderId(long id) {
		String query = "SELECT name FROM JournalFolder journal where folderid=" + id;
		return journalFolderDao.findByQuery(query).get(0);
	}
}
