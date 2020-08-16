package com.portal.service;

import java.util.ArrayList;

public interface WeatherService {
	 public void getPageLinks(String URL);
	 public String[] parseArticle();
	 public String[] getArticles();
	 public String[] getEngArticles();
	 public String[] get10DayMyanmarArticles();
}
