package com.portal.entity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateUtil {

	private static final SimpleDateFormat withSlash = new SimpleDateFormat("dd/MM/yyyy");
	private static final SimpleDateFormat withDash = new SimpleDateFormat("dd-MM-yyyy");
	private static final SimpleDateFormat withDot = new SimpleDateFormat("dd.MM.yyyy");

	private static Logger logger = Logger.getLogger(DateUtil.class);

	public static boolean isEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}

	public static Date addMonth(Date date, int numberOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, numberOfMonth);
		return calendar.getTime();
	}

	public static String getCalendarMonthName(int month) {
		if (month >= 12)
			month -= 12;

		switch (month) {
		case Calendar.JANUARY:
			return "JANUARY";
		case Calendar.FEBRUARY:
			return "FEBRUARY";
		case Calendar.MARCH:
			return "MARCH";
		case Calendar.APRIL:
			return "APRIL";
		case Calendar.MAY:
			return "MAY";
		case Calendar.JUNE:
			return "JUNE";
		case Calendar.JULY:
			return "JULY";
		case Calendar.AUGUST:
			return "AUGUST";
		case Calendar.SEPTEMBER:
			return "SEPTEMBER";
		case Calendar.OCTOBER:
			return "OCTOBER";
		case Calendar.NOVEMBER:
			return "NOVEMBER";
		case Calendar.DECEMBER:
			return "DECEMBER";
		default:
			return null;
		}
	}
	

	public static Date parseDate(String dateStr) {
		if (isEmpty(dateStr))
			return null;
		dateStr = dateStr.replaceAll(" ", "");
		if (dateStr.equals(""))
			return null;
		if (dateStr.length() > 10)
			return null;
		dateStr = dateStr.replaceAll("[-.]+", "/");
		try {
			if (dateStr.contains("/") || dateStr.contains(".") || dateStr.contains("-")) {
				if (dateStr.contains("/")) {
					if (dateStr.indexOf("/") == dateStr.lastIndexOf("/")) {
						if (dateStr.length() > 7)
							return null;
						return withSlash.parse("01/" + dateStr);
					}
					return withSlash.parse(dateStr);
				} else if (dateStr.contains("-")) {
					if (dateStr.indexOf("-") == dateStr.lastIndexOf("-")) {
						if (dateStr.length() > 7)
							return null;
						logger.info("Parser with dash");
						String input = "01-" + dateStr;
						Date date = withDash.parse(input);
						logger.info("Input :" + input);
						logger.info("Out put: " + withDash.format(date));
						return date;
					}
					return withDash.parse(dateStr);
				} else {
					if (dateStr.indexOf(".") == dateStr.lastIndexOf(".")) {
						if (dateStr.length() > 7)
							return null;
						return withDot.parse("01." + dateStr);
					}
					return withDot.parse(dateStr);
				}
			}
			return withDash.parse("01-01-" + dateStr);
		} catch (Exception e) {
			logger.error("Can't parse date" + dateStr);
			return null;
		}
	}

}
