package com.portal;

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;

import com.portal.parsing.DocumentParsing;

public class Sample {
	public static void main(String[] args) {
		String contact="<?xml version=\"1.0\"?><root available-locales=\"en_US,my_MM\" default-locale=\"en_US\"><dynamic-element name=\"content\" type=\"text_area\" index-type=\"text\" instance-id=\"vzgu\"><dynamic-content language-id=\"en_US\"><![CDATA[<div class=\"journal-content-article\"><p>&nbsp;</p><div class=\"journal-content-article\"><ul class=\"row contact_add_bar\"><li>&nbsp;</li><li><h2 class=\"title\">Myanmar National Portal</h2><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">Ministry of Transport and Communications</b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">Information Technology and Cyber Security Department</b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">S12, MPT Exchange Building</b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">Zabu Kyetthayay Road,Nay Pyi Taw,Myanmar.</b></b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">phone: 067-3422436</b></b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">email:&nbsp;&nbsp;webmaster@myanmar.gov.mm</b></b></p></li></ul></div></div>]]></dynamic-content><dynamic-content language-id=\"my_MM\"><![CDATA[<div class=\"journal-content-article\"><p>&nbsp;</p><div class=\"journal-content-article\"><ul class=\"row contact_add_bar\"><li>&nbsp;</li><li><h2 class=\"title\">Myanmar National Portal</h2><p>&nbsp;</p><p><meta charset=\"utf-8\" /></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">MPT S12 Exchange အဆောက်အဦ</b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\"><b><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">"
				+ "ဇမ္ဗူ့ကျက်သရေလမ်း၊ နေပြည်တော်၊ မြန်မာ။</b></b></b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\"><b><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">"
				+ "ဖုန်းနံပါတ်: ၀၆၇ - ၃၄၂၂၄၃၆</b></b></b></p><p dir=\"ltr\"><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\"><b><b id=\"docs-internal-guid-7c6af4bb-0725-a3c6-7df8-c0e513d11211\">"
				+ "အီးမေးလ်: </b></b></b>webmaster@myanmar.gov.mm</p></li></ul></div></div>]]></dynamic-content></dynamic-element></root>";
		String[] engmyan = new DocumentParsing().ParsingContent(contact);
		String engContent = engmyan[0];
		String myanContent = engmyan[1];
		Document engdoc = Jsoup.parse(engContent, "", Parser.htmlParser());
		Elements engpara = engdoc.getElementsByTag("p");
		Document myandoc = Jsoup.parse(myanContent, "", Parser.htmlParser());
		Elements myanpara = myandoc.getElementsByTag("p");
		String addr = "",phoneno="",email="";
		String myanaddr = "",myanphoneno="",myanemail="";
		for(Element e : engpara) {
				if (e.text().toString().contains("phone")) {
					 phoneno = e.text().toString();
				}
				if (e.text().toString().contains("email")) {
					 email = e.text().toString();
				}
				if (!e.text().toString().contains("email") && !e.text().toString().contains("phone")) {
					String ele =e.text().toString();
					if(!ele.equals(""))
						if(!addr.equals(""))
							addr =addr +"\n" + e.text().toString();
						else addr = e.text().toString();
				}
		}
		for(Element e : myanpara) {
			if (e.text().toString().contains("၀")) {
				myanphoneno = e.text().toString();
			}
			if (e.text().toString().contains("@")) {
				myanemail = e.text().toString();
			}
			if (!e.text().toString().contains("@") && !e.text().toString().contains("၀")) {
				String ele =e.text().toString();
				if(!ele.equals(""))
					if(!myanaddr.equals(""))
						myanaddr =myanaddr +"\n" + e.text().toString();
					else myanaddr = e.text().toString();
			}
	}
		System.out.println("Address "+ addr);
		System.out.println("Email :"+ email);
		System.out.println("Phone No :"+ phoneno);
		System.out.println("Address "+ myanaddr);
		System.out.println("Email :"+ myanemail);
		System.out.println("Phone No :"+ myanphoneno);
	}
}
