package com.portal.dao;

import java.util.List;

import javax.naming.ServiceUnavailableException;

import com.portal.entity.CalendarBooking;
import com.portal.entity.JournalArticle;

public interface CalendarBookingDao extends AbstractDao<CalendarBooking, String> {

	public List<CalendarBooking> findByQueryStringWithSize(String queryString, int size);

	public List<Object> byQueryString(String queryString);
	

}
