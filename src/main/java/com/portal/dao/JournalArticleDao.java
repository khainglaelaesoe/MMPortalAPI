package com.portal.dao;

import java.util.List;

import javax.naming.ServiceUnavailableException;

import com.portal.entity.JournalArticle;

public interface JournalArticleDao extends AbstractDao<JournalArticle, String> {
	public void save(JournalArticle journalArticle) throws ServiceUnavailableException;

	public List<JournalArticle> findByQueryStringWithSize(String queryString, int size);

	public List<Object> byQueryString(String queryString);
	
	public List<String> findByQuery(String queryString);

}
