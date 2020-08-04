package com.portal.dao.impl;

import java.util.List;

import javax.naming.ServiceUnavailableException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.portal.dao.JournalArticleDao;
import com.portal.entity.JournalArticle;

@Repository
public class JournalArticleDaoImpl extends AbstractDaoImpl<JournalArticle, String> implements JournalArticleDao {

	@Autowired
	private EntityManager entityManager;

	protected JournalArticleDaoImpl() {
		super(JournalArticle.class);
	}

	@Override
	public List<JournalArticle> findByQueryStringWithSize(String queryString, int size) {
		return entityManager.createNativeQuery(queryString).getResultList();
	}

	@Override
	public void save(JournalArticle journalArticle) throws ServiceUnavailableException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<Object> byQueryString(String queryString) {
		return super.findByQueryString(queryString);
	}

}
