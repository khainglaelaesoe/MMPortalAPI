package com.portal.parsing;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base32;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import com.portal.controller.DiscussionController;
import com.portal.entity.TableData;

public class DocumentParsing {

	private static Logger logger = Logger.getLogger(DocumentParsing.class);

	public String[] ParsingContent(String input) {
		String[] engmyan = new String[2];
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());

		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("text_area")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan[0] = ImageSourceChange(enginput);
				}

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan[1] = ImageSourceChange(myaninput);

				}
			}
		}
		return engmyan;

	}

	public String[] ParsingTable(String input) {
		String[] engmyan = new String[2];
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("text_area")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan[0] = enginput;
				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan[1] = myaninput;
					// engmyan[1] = ImageSourceChange(myaninput);
					// engmyan[1] = element.getElementsByAttributeValueContaining("language-id",
					// "my_MM").text();

				} // if
			}

		}

		return engmyan;

	}

	public String AvailableLanguage(String input) {
		String language = "";
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("root");
		for (Element element : elements) {
			if (element.attr("available-locales").equals("my_MM"))
				language = "my_MM";
			else if (element.attr("available-locales").equals("en_US"))
				language = "en_US";
			else
				language = "en_US,my_MM";
		}
		return language;
	}

	public String ParsingImage(String input) {
		String imagepath = "";
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		int i = 0;
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.hasText() == true) {
					imagepath = SourceChange(element.text());
				}
			}
		}
		return imagepath;
	}

	public List<String> ParsingEngImage(String input) {
		String imagepath = "";
		List images = new ArrayList<String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					imagepath = SourceChange(element.getElementsByAttributeValueContaining("language-id", "en_US").text());
					images.add(imagepath);
				}
			}
		}
		return images;
	}

	public List<String> ParsingEngImage2(String input) {
		String imagepath = "";
		List images = new ArrayList<String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					imagepath = SourceChange2(element.getElementsByAttributeValueContaining("language-id", "en_US").text());
					images.add(imagepath);
				}
			}
		}
		return images;
	}

	public List<String> ParsingMyanImage(String input) {
		String imagepath = "";
		List images = new ArrayList<String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", " my_MM").size() > 0) {
					imagepath = SourceChange(element.getElementsByAttributeValueContaining("language-id", " my_MM").text());
					images.add(imagepath);
				}
			}
		}
		return images;
	}

	public List<String> ParsingMyanImage2(String input) {
		String imagepath = "";
		List images = new ArrayList<String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", " my_MM").size() > 0) {
					imagepath = SourceChange2(element.getElementsByAttributeValueContaining("language-id", " my_MM").text());
					images.add(imagepath);
				}
			}
		}
		return images;
	}

	public String ParsingSpan(String input) {
		Document doc = Jsoup.parse(input, "", Parser.htmlParser());
		Elements elements = doc.select("span");
		for (Element element : elements) {
			if (element.attr("style").startsWith("color")) {
				element.removeAttr("style");
			}
			if (element.attr("style").startsWith("font-size")) {
				element.removeAttr("style");
			}
			if (element.attr("style").startsWith("background-color")) {
				element.removeAttr("style");
			}
		}

		Elements elements2 = doc.select("img");
		for (Element element : elements2) {
			element.removeAttr("alt");
			if (element.attr("style").startsWith("width")) {
				element.removeAttr("style");
			}
			if (element.attr("style").startsWith("height")) {
				element.removeAttr("style");
			}
		}
		return doc.html();

	}

	public ArrayList<String> ParsingAllContent(String input) {
		ArrayList<String> engmyan = new ArrayList<String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		int i = 0;
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("text_area")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan.add(ImageSourceChange(enginput));

				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan.add(ImageSourceChange(myaninput));

				} // if
			}

			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					if (element.getElementsByAttributeValueContaining("language-id", "en_US").hasText() == true) {
						String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
						engmyan.add(SourceChange(enginput));

					}
				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					if (element.getElementsByAttributeValueContaining("language-id", "my_MM").hasText() == true) {
						String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
						engmyan.add(SourceChange(myaninput));
					}
				} // if
			}

			if (element.attr("type").equals("document_library")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {

					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan.add(AudioSourceChange(enginput));

				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan.add(AudioSourceChange(myaninput));

				} // if
			}

		}
		return engmyan;
	}

	public ArrayList<String> ParsingAllContent2(String input) {
		ArrayList<String> engmyan = new ArrayList<String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		int i = 0;
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("text_area")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan.add(ImageSourceChange2(enginput));

				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan.add(ImageSourceChange2(myaninput));

				} // if
			}

			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					if (element.getElementsByAttributeValueContaining("language-id", "en_US").hasText() == true) {
						String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
						engmyan.add(SourceChange(enginput));
					}
				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {

					if (element.getElementsByAttributeValueContaining("language-id", "my_MM").hasText() == true) {
						String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
						engmyan.add(SourceChange(myaninput));
					}
				} // if
			}

			if (element.attr("type").equals("document_library")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {

					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan.add(AudioSourceChange(enginput));

				} // if

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan.add(AudioSourceChange(myaninput));

				} // if
			}

		}
		return engmyan;
	}

	/*
	 * public String SourceChange(String content) { return "https://myanmar.gov.mm"
	 * + content; }
	 */

	public String SourceChange(String content) {
		if(!content.contains("http")) {
			content = "<img src=" + "\"https://myanmar.gov.mm" + content + "\">";
		}else 
			content = "<img src=" + content + ">";
		
		return content;
	}

	public String SourceChange2(String content) {
		content = "https://myanmar.gov.mm" + content;
		return content;
	}

	public String AudioSourceChange(String content) {
		content = "<audio>https://myanmar.gov.mm" + content;
		content = content + "</audio>";
		return content;
	}

	public String ImageSourceChange(String htmlinput) {

		Document docimage = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		String imgreplace = "";
		Elements images = docimage.getElementsByTag("img");
		if (images.size() > 0) {
			for (Element img : images) {
				String imgsrc = img.attr("src");
				System.out.println("Source Change Content........" + imgsrc);
				if (imgsrc.startsWith("data:image/png;base64")) {
					imgreplace = imgsrc;

				} else {
					imgreplace = "https://myanmar.gov.mm" + imgsrc;
					img.attr("src", imgreplace);
				}
			}
		}
		return docimage.html();
	}

	public String ImageSourceChange2(String htmlinput) {

		Document docimage = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		Elements images = docimage.getElementsByTag("a");
		if (images.size() > 0) {
			for (Element img : images) {
				String imgsrc = img.attr("href");
				String imgreplace = imgsrc.startsWith("/document") ? "https://myanmar.gov.mm" + imgsrc : imgsrc;
				img.attr("href", imgreplace);
			}
		}
		return docimage.html();
	}

	public List<String> SelectParagraph(String htmlinput) {
		List<String> finalresult = new ArrayList<String>();
		Document doc = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		Elements para = doc.getElementsByTag("p");
		Elements para1 = doc.getElementsByTag("h2");
		String title = para1.toString();
		for (Element p : para) {
			String res4 = p.toString().replace("<p>&nbsp;</p>", "");
			String res0 = res4.toString().replace("</p>", "");
			String res1 = res0.toString().replace("</p>", "");
			String res2 = res1.toString().replace("<p>", "");
			String res3 = res2.toString().replace("<p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">", "");
			String res6 = res3.toString().replace("<b><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">", "");
			String res7 = res6.toString().replace("</b>", "");
			String res8 = res7.toString().replace("<b>", "");
			String res9 = res8.toString().replace("<br>", "");
			finalresult.add(res9);
		}
		finalresult.add(title);

		return finalresult;
	}

	public List<String> SelectParagraph1(String htmlinput) {
		List<String> finalresult = new ArrayList<String>();
		Document doc = Jsoup.parse(htmlinput, "", Parser.htmlParser());
		Elements para = doc.getElementsByTag("p");
		String para1 = para.toString().replaceAll("\\<.*?\\>", "");
		Elements title = doc.getElementsByTag("h2");
		String title1 = title.toString();

		finalresult.add(title1);
		finalresult.add(para1);

		return finalresult;
	}

	public List<String> SelectTable(String htmlinput) {
		TableData tblData = new TableData();
		List<String> result = new ArrayList<String>();
		final InputStream html = getClass().getClassLoader().getResourceAsStream("table.html");
		Document doc;
		return result;
	}

	public String[] ParsingTitle(String input) {
		String[] engmyan = new String[2];
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("root");
		for (Element element : elements) {
			engmyan[0] = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
			engmyan[1] = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
		}
		return engmyan;
	}

	public String[] ParsingContentImage(String input) {
		String[] engMyan = new String[2];
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myanImage = "https://myanmar.gov.mm" + element.text().toString();
					engMyan[0] = myanImage;
				}
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					String engImage = "https://myanmar.gov.mm" + element.text().toString();
					engMyan[1] = engImage;
				}
			}
		}
		return engMyan;
	}

	public List<Map<String, String>> ParsingImageTextTextArea(String input) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String engimage = "", myanimage = "";
		String engtext = "", myantext = "";
		String engtextarea = "", myantextarea = "";
		Map<String, String> engmyanimage = new HashMap<String, String>();
		Map<String, String> engmyantext = new HashMap<String, String>();
		Map<String, String> engmyantextarea = new HashMap<String, String>();
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());
		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("image")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					engimage = SourceChange(element.getElementsByAttributeValueContaining("language-id", "en_US").text());
				}
				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					myanimage = SourceChange(element.getElementsByAttributeValueContaining("language-id", "my_MM").text());
				}

			}

			if (element.attr("type").equals("text")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					engtext = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
				}

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					myantext = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
				}

			}
			if (element.attr("type").equals("text_area")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					engtextarea = imageChange(element.getElementsByAttributeValueContaining("language-id", "en_US").text());
				}

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					myantextarea = imageChange(element.getElementsByAttributeValueContaining("language-id", "my_MM").text());
				}

			}

		}
		engimage = !engimage.isEmpty() ? engimage : myanimage;
		myanimage = !myanimage.isEmpty() ? myanimage : engimage;
		engmyanimage.put("engimage", engimage);
		engmyanimage.put("myanimage", myanimage);
		list.add(engmyanimage);

		engtext = !engtext.isEmpty() ? engtext : myantext;
		myantext = !myantext.isEmpty() ? myantext : engtext;
		engmyantext.put("engtext", engtext);
		engmyantext.put("myantext", myantext);
		list.add(engmyantext);

		engtextarea = !engtextarea.isEmpty() ? engtextarea : myantextarea;
		myantextarea = !myantextarea.isEmpty() ? myantextarea : engtextarea;
		engmyantextarea.put("engtextarea", engtextarea);
		engmyantextarea.put("myantextarea", myantextarea);
		list.add(engmyantextarea);
		return list;
	}

	public String imageChange(String htmlinput) {
		String resdata = "";
		Document docimage = Jsoup.parse(htmlinput, "", Parser.xmlParser());
		String imgreplace = "";
		Elements images = docimage.getElementsByTag("img");
		if (images.size() > 0) {
			for (Element img : images) {
				String imgsrc = img.attr("src");
				System.out.println("Source Change Content........" + imgsrc);
				if (imgsrc.startsWith("data:image/png;base64")) {
					imgreplace = imgsrc;

				} else {
					imgreplace = "https://myanmar.gov.mm" + imgsrc;
					img.attr("src", imgreplace);
				}
			}
			resdata = images.toString();
		} else
			resdata = docimage.text().toString();
		return resdata;
	}

	public String[] Parsingdocument_library(String input) {
		String[] engmyan = new String[2];
		Document doc = Jsoup.parse(input, "", Parser.xmlParser());

		Elements elements = doc.select("dynamic-element");
		for (Element element : elements) {
			if (element.attr("type").equals("document_library")) {
				if (element.getElementsByAttributeValueContaining("language-id", "en_US").size() > 0) {
					String enginput = element.getElementsByAttributeValueContaining("language-id", "en_US").text();
					engmyan[0] = "https://myanmar.gov.mm" + enginput;
				}

				if (element.getElementsByAttributeValueContaining("language-id", "my_MM").size() > 0) {
					String myaninput = element.getElementsByAttributeValueContaining("language-id", "my_MM").text();
					engmyan[1] = "https://myanmar.gov.mm" + myaninput;

				}
			}
		}
		return engmyan;

	}
}
