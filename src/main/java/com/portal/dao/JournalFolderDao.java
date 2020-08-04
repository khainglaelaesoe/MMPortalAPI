package com.portal.dao;

import java.util.List;

import com.portal.entity.JournalFolder;

public interface JournalFolderDao  extends AbstractDao<JournalFolder, String> {

	List<String> findByQuery(String query);

}
