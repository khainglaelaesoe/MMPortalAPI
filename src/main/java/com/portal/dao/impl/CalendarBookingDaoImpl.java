package com.portal.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.portal.dao.CalendarBookingDao;
import com.portal.entity.CalendarBooking;

@Repository
public class CalendarBookingDaoImpl extends AbstractDaoImpl<CalendarBooking, String> implements CalendarBookingDao {

	@Autowired
	private EntityManager entityManager;

	protected CalendarBookingDaoImpl() {
		super(CalendarBooking.class);
	}

	@Override
	public List<CalendarBooking> findByQueryStringWithSize(String queryString, int size) {
		return entityManager.createNativeQuery(queryString).getResultList();
	}


	@Override
	public List<Object> byQueryString(String queryString) {
		return super.findByQueryString(queryString);
	}

}
