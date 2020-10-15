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
import com.portal.entity.Weather;
import com.portal.parsing.DocumentParsing;
import com.portal.service.CalenderBookingService;
import com.portal.service.WeatherService;

@Controller
@RequestMapping("weather")
public class WeatherController extends AbstractController {

	@Autowired
	private WeatherService weatherService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	@RequestMapping(value = "getWeather", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject getWeather(@RequestHeader("Authorization") String encryptedString) {
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

		Weather weather = new Weather();

		weatherService.getPageLinks("https://www.moezala.gov.mm/my/daily-weather-forecast%20");
		String[] myanoutput = weatherService.parseArticle();
		weather.setMyantitle(myanoutput[0]);
		weather.setMyancontent(myanoutput[1]);

		weatherService.getPageLinks("https://www.moezala.gov.mm/daily-weather-forecast%20");
		String[] engoutput = weatherService.parseArticle();
		weather.setEngtitle(engoutput[0]);
		weather.setEngcontent(engoutput[1]);
		resultJson.put("DailyWeather", weather);

		Weather tenday = new Weather();
		System.out.println("========================10day Myanmar=========================");
		weatherService.getPageLinks("https://moezala.gov.mm/my/10-days-weather-forecast%20");
		String[] tenmyanoutput = weatherService.parseArticle();
		tenday.setMyantitle(tenmyanoutput[0]);
		tenday.setMyancontent(tenmyanoutput[1]);

		System.out.println("========================10day English=========================");
		weatherService.getPageLinks("https://moezala.gov.mm/10-days-weather-forecast%20");
		String[] tenengoutput = weatherService.parseArticle();
		tenday.setEngtitle(tenengoutput[0]);
		tenday.setEngcontent(tenengoutput[1]);
		resultJson.put("10DayWeather", tenday);
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
			SimpleDateFormat simpleformat = new SimpleDateFormat("MMMM YYYY");
			String strMonthYear = simpleformat.format(startDate);
			SimpleDateFormat simpleformat1 = new SimpleDateFormat("dd MMM");
			String strDayMonth = simpleformat1.format(startDate);
			// endDate
			Date endDate1 = new Date(cl.getEndtime());
			Date endDate = subtractDays(endDate1);
			SimpleDateFormat simpleformat2 = new SimpleDateFormat("MMMM YYYY");
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

	public static Date subtractDays(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}
}
