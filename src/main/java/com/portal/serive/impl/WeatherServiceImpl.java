package com.portal.serive.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.portal.entity.Weather;
import com.portal.service.WeatherService;

@Service("weatherService")
public class WeatherServiceImpl implements WeatherService {

	private HashSet<String> links;
	private List<List<String>> articles;

	public WeatherServiceImpl() {
		links = new HashSet<>();
		articles = new ArrayList<>();
	}

	public void getPageLinks(String URL) {
		links.clear();
		if (!links.contains(URL)) {
			try {
				Document document = Jsoup.connect(URL).get();
				Elements spans = document.select("div.region-inner.region-content-inner").select("span");

				for (Element span : spans) {
					System.out.println("span........." + span.text());
					Element link = span.select("a").first();

					if (link != null) {
						System.out.println("link in 10 days........" + link);
						links.add(link.absUrl("href"));
						break;
					}

				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

	}

	// Connect to each link saved in the article and find all the articles in the
	// page
	public String[] getArticles() {
		String[] output = new String[2];
		links.forEach(x -> {
			Document document;
			try {
				document = Jsoup.connect(x).get();
				//Elements paras = document.select("article p");
				Elements paras = document.select("div.region-inner.region-content-inner").select("p");
				String title = "";
				String content = "";
				
				for (Element p : paras) {
					if (p.hasClass("rtecenter")) {
						Elements spans = p.getElementsByTag("span");
						for (Element span : spans) {
							// if(span.children()!=null)System.out.println("Span
							// children...."+span.children().size());
							if (span.children().size() == 0)
								title += span.text();
							if (span.children().size() > 8)
								title += "\n";

						}

					} else {
						Elements spans = p.getElementsByTag("span");

						for (Element span : spans) {
							if (span.children().size() == 0)
								content += span.text();
							if (span.children().size() > 2)
								content += "\n";

						}

					} // else
				} // for
				output[0] = title;
				output[1] = content;

			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		});
		return output;
	}

	public String[] parseArticle() {
		String[] output = new String[2];
		links.forEach(x -> {
			Document document;
			try {
				document = Jsoup.connect(x).get();
				Elements paras = document.select("article p");
				String title = "";
				String content = "";
				
				for (Element p : paras) {
					if (p.hasClass("rtecenter")) {
						title += p.text()+"<br>";
					}
					else
						content += p.text()+"<br>";
				} // for
				
				output[0] = title;
				output[1] = content;
				System.out.println("title........." + output[0]);
				System.out.println("content........." + output[1]);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}

		});
		return output;
	}

	public String[] get10DayMyanmarArticles() {
		String[] output = new String[2];
		links.forEach(x -> {
			Document document;
			try {
				document = Jsoup.connect(x).get();
				Elements paras = document.select("article p");
				String title = "";
				String content = "";
				for (Element p : paras) {
					if (p.hasClass("rtecenter")) {
						Elements spans = p.getElementsByTag("span");
						for (Element span : spans) {
							// if(span.children()!=null)System.out.println("Span
							// children...."+span.children().size());
							if (span.children().size() == 0)
								title += span.text();
							if (span.children().size() > 8)
								title += "\n";

						}

					} else {
						Elements spans = p.getElementsByTag("span");

						for (Element span : spans) {
							if (span.getElementsByAttributeValueContaining("style", "color").size() > 0) {
								content += "\n";
							} else if (span.children().size() == 0) {
								String value = span.text().substring(0, 1);
								String fix = span.text().substring(1, 2);
								if (fix.equals("။") == true)
									System.out.println("value......" + value);
								boolean number = isNumeric(value);
								if (number = true && fix.equals("။") == true)
									content += "\n";
								content += span.text();

							} else {
							}

						}

					} // else
				} // for
				output[0] = title;
				output[1] = content;
				System.out.println("title........." + output[0]);
				System.out.println("content........." + output[1]);

			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		});
		return output;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public String[] getEngArticles() {
		String[] output = new String[2];
		links.forEach(x -> {
			Document document;
			try {
				document = Jsoup.connect(x).get();
				Elements paras = document.select("article p");
				String title = "";
				String content = "";
				for (Element p : paras) {
					if (p.hasClass("rtecenter")) {
						Elements spans = p.getElementsByTag("span");
						for (Element span : spans) {

							if (span.children().size() == 0) {
								if (span.getElementsByTag("strong") != null)
									title += span.text() + "\n";
								else
									title += span.text() + "\n";
							}

						}

					} else {
						content += p.text() + "\n";

					} // else
				} // for
				output[0] = title;
				output[1] = content;

			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		});
		return output;
	}

}
