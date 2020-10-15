package com.portal.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.AES;
import com.portal.entity.CalendarBooking;
import com.portal.entity.Views;
import com.portal.parsing.DocumentParsing;
import com.portal.service.CalenderBookingService;

@Controller
@RequestMapping("upcoming")
public class UpcomingController extends AbstractController{

	@Autowired
	private CalenderBookingService calendarService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	@RequestMapping(value = "upcomingholidays", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject upcomingholidays(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		resultJson.put("Holiday", parseHoliday(calendarService.getCalendarbookingforHoliday()));
		return resultJson;
	}

	@RequestMapping(value = "getUpcomingholid‌ays", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getUpcomingholidays(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		resultJson.put("Holiday", parseHoliday(calendarService.getCalendarbookingforHoliday()));
		return resultJson;
	}

	private List<CalendarBooking> parseHoliday(List<CalendarBooking> clendarList) {
		List<CalendarBooking> clList = new ArrayList<CalendarBooking>();
		for (CalendarBooking cl : clendarList) {
			CalendarBooking clres = new CalendarBooking();
			// holidays
			String[] title = new DocumentParsing().ParsingTitle(cl.getTitle());
			String engTitle = title[0];
			String mmTitle = title[1];
			// StartDate
			Date startDate = new Date(cl.getStarttime());
			SimpleDateFormat simpleformat = new SimpleDateFormat("MMMM yyyy");
			String strMonthYear = simpleformat.format(startDate);
			SimpleDateFormat simpleformat1 = new SimpleDateFormat("dd MMM");
			String strDayMonth = simpleformat1.format(startDate);
			// endDate
			Date endDate1 = new Date(cl.getEndtime());
			Date endDate = subtractDays(endDate1);
			SimpleDateFormat simpleformat2 = new SimpleDateFormat("MMMM yyyy");
			String strMonthYearEndDate = simpleformat2.format(endDate);
			SimpleDateFormat simpleformat3 = new SimpleDateFormat("dd MMM");
			String strDayMonthEndDate = simpleformat3.format(endDate);
			clres.setMmTitle(mmTitle);
			clres.setEngTitle(engTitle);
			clres.setStartmonthyear(strMonthYear);
			clres.setStartdaymonth(strDayMonth);
			clres.setEndmonthyear(strMonthYearEndDate);
			clres.setEnddaymonth(strDayMonthEndDate);
			clList.add(clres);
		}

		return clList;
	}

	@RequestMapping(value = "getUpcomingEvents", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getUpcomingEvent(@RequestHeader("Authorization") String encryptedString) {
		JSONObject resultJson = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Authorization failure!");
				return resultJson;
			}
		} catch (Exception e) {
			resultJson.put("status", 0);
			resultJson.put("message", "Authorization failure!");
			return resultJson;
		}
		resultJson.put("Holiday", parseHoliday(calendarService.getCalendarbookingforEvent()));
		return resultJson;
	}
	public static Date subtractDays(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}
}
