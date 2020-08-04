package com.portal.service;

import java.util.List;

import com.portal.entity.CalendarBooking;

public interface CalenderBookingService {

	List<CalendarBooking> getCalendarbookingforHoliday();

	List<CalendarBooking> getCalendarbookingforEvent();

}
