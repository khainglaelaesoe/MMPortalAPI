package com.portal.serive.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.CalendarBookingDao;
import com.portal.entity.CalendarBooking;
import com.portal.service.CalenderBookingService;

@Service("calenderBookingService")
public class ClanderBookingServiceImpl implements CalenderBookingService {

	@Autowired
	private CalendarBookingDao calendarDao;

	@Override
	public List<CalendarBooking> getCalendarbookingforHoliday() {
		List<Object> objectList;
		List<CalendarBooking> arrlist = new ArrayList<CalendarBooking>();
		String queryStr = "Select title,starttime,endtime from CalendarBooking where calendarId=48848 and FROM_UNIXTIME(startTime/1000) > DATE(NOW()) order by starttime";
		objectList = calendarDao.findByQueryString(queryStr);
		for (Object object : objectList) {
			CalendarBooking cl =new CalendarBooking();
			Object[] obj = (Object[]) object;
			if (obj[0] == null || obj[1] == null || obj[1] == null)
				return null;

			cl.setTitle(obj[0].toString());
			cl.setStarttime(Long.parseLong(obj[1].toString()));
			cl.setEndtime(Long.parseLong(obj[2].toString()));
			arrlist.add(cl);
		}
		return arrlist;
	}
	
	@Override
	public List<CalendarBooking> getCalendarbookingforEvent() {
		List<Object> objectList;
		List<CalendarBooking> arrlist = new ArrayList<CalendarBooking>();
		String queryStr = "Select title,starttime,endtime,description from CalendarBooking where calendarId=48843 and FROM_UNIXTIME(startTime/1000) > DATE(NOW()) order by starttime";
		objectList = calendarDao.findByQueryString(queryStr);
		for (Object object : objectList) {
			CalendarBooking cl =new CalendarBooking();
			Object[] obj = (Object[]) object;
			if (obj[0] == null || obj[1] == null || obj[1] == null)
				return null;

			cl.setTitle(obj[0].toString());
			cl.setStarttime(Long.parseLong(obj[1].toString()));
			cl.setEndtime(Long.parseLong(obj[2].toString()));
			cl.setDescription(obj[3].toString());
			arrlist.add(cl);
		}
		return arrlist;
	}
	
}
