/*
 * BizLeap CONFIDENTIAL
 * ____________________
 *
 * Copyright 2014 - 2015 BizLeap Technology
 *
 * All Rights Reserved. www.bizleap.com
 *
 * NOTICE: All information contained herein is, and remains
 * the property of BizLeap Technology or its group
 * companies. The intellectual and technical concepts contained
 * herein are proprietary to BizLeap Technology may be covered by Myanmar and
 * Foreign Patents, patents in process, and are protected by
 * trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from BizLeap Technology. Any
 * reproduction of this material must contain this notice
 */
package com.portal.dao.impl;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import com.mchange.rmi.ServiceUnavailableException;
import com.portal.dao.AbstractDao;

/**
 * @author
 * @since 1.0.0
 */
@Transactional
public abstract class AbstractDaoImpl<E, I extends Serializable> implements AbstractDao<E, I> {

	private Class<E> entityClass;

	@Autowired
	private EntityManager entityManager;

	private static Logger logger = Logger.getLogger(AbstractDaoImpl.class);

	protected AbstractDaoImpl(Class<E> entityClass) {
		this.entityClass = entityClass;
	}

	public List<E> getAll(String queryString) {
		List<E> entityList;
		Query query = entityManager.createQuery(queryString);
		entityList = query.getResultList();		
		return entityList;
	}

	public List<Object> findByQueryString(String queryString) {
		List<Object> entityList;
		Query query = entityManager.createQuery(queryString);
		entityList = query.getResultList();
		return entityList;
	}

	public List<Long> findLongByQueryString(String queryString) {
		List<Long> entityList;
		Query query = entityManager.createQuery(queryString);
		entityList = query.getResultList();
		return entityList;
	}

	public List<Integer> findIntByQueryString(String queryString) {
		List<Integer> entityList;
		Query query = entityManager.createQuery(queryString);
		entityList = query.getResultList();
		return entityList;
	}

	public List<Double> findDoubleByQueryString(String queryString) {
		List<Double> entityList;
		Query query = entityManager.createQuery(queryString);
		entityList = query.getResultList();
		return entityList;
	}

	public List<Long> findLongByQueryString(String queryString, String dataInput) {
		List<Long> entityList;
		Query query = entityManager.createQuery(queryString).setParameter("dataInput", dataInput);
		entityList = query.getResultList();
		return entityList;
	}

	public List<Object> findByQueryString(String queryString, String dataInput) {
		List<Object> entityList;
		Query query = entityManager.createQuery(queryString).setParameter("dataInput", dataInput);
		entityList = query.getResultList();
		return entityList;
	}

	public List<String> findByQuery(String queryString) {
		List<String> entityList;
		Query query = entityManager.createNativeQuery(queryString);
		entityList = query.getResultList();
		return entityList;
	}

	public List<E> byQuery(String queryString) {
		List<E> entityList;
		Query query = entityManager.createQuery(queryString);
		entityList = query.getResultList();
		return entityList;
	}

	@Override
	public List<String> findByDateRange(String queryString, String startDate, String endDate) {
		List<String> entityList;
		Query query = entityManager.createQuery(queryString).setParameter("dataInput1", startDate).setParameter("dataInput2", endDate);
		entityList = query.getResultList();
		for (String entity : entityList)
			Hibernate.initialize(entity);
		return entityList;
	}

	public Session getSession() {
		return entityManager.unwrap(Session.class);
	}

	public boolean checkSaveOrUpdate(E e) throws ServiceUnavailableException {
		try {
			Session session = getSession();
			session.saveOrUpdate(e);
			session.flush();
		} catch (CannotCreateTransactionException exception) {
			logger.error("Exception: " + exception);
			throw new ServiceUnavailableException();
		}
		return true;
	}

	@Override
	public List<E> findById(String queryString) {
		Query query = entityManager.createQuery(queryString);
		return query.getResultList();
	}

	@Override
	public String validpwd(String queryString) {
		Query query = entityManager.createQuery(queryString);
		return (String) query.getSingleResult();
	}
	
	@Override
	public boolean saveUpdate(E e) throws ServiceUnavailableException {
		try {
			Session session = getSession();
			session.saveOrUpdate(e);
			session.flush();
		} catch (CannotCreateTransactionException exception) {
			logger.error("Exception: " + exception);
			throw new ServiceUnavailableException();
		}
		return true;
	}

	public void saveOrUpdate(E e) throws ServiceUnavailableException {
		try {
			Session session = getSession();
			session.saveOrUpdate(e);
			session.flush();
		} catch (CannotCreateTransactionException exception) {
			logger.error("Exception: " + exception);
			throw new ServiceUnavailableException();
		}
	}
	public List<E> findDatabyQueryString(String queryString, long dataInput) {
		List<E> entityList;
		Query query = entityManager.createQuery(queryString).setParameter("dataInput", dataInput);
		entityList = query.getResultList();
		return entityList;
	}
	
	public int findCountByQueryString(String queryString) {
		Query query = entityManager.createQuery(queryString);
		return query.getSingleResult() != null ? Integer.parseInt(query.getSingleResult().toString()) : 0;
	}
	public long findLong(String queryString) {
		long id = 0;
		try {
			Query query = entityManager.createQuery(queryString);
			id = (long) query.getSingleResult();
		}catch(NoResultException nre){
			
		}
		if(id == 0){
			
		}
		return id;
	}
}
