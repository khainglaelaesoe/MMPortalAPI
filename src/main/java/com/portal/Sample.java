package com.portal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.portal.parsing.DocumentParsing;

public class Sample {
	public static void main(String[] args) {
		//String content="<?xml version=\"1.0\"?><root available-locales=\"en_US,my_MM\" default-locale=\"en_US\"><dynamic-element name=\"image\" type=\"image\" index-type=\"text\" instance-id=\"bofq\"><dynamic-content language-id=\"en_US\" alt=\"\" name=\"0-02-06-ed9bf22df63aba20b39193e5dc6da9c05359e07a05ffe55d8b08c1c13fab72d3_f97e3e4a (3).jpg\" title=\"0-02-06-ed9bf22df63aba20b39193e5dc6da9c05359e07a05ffe55d8b08c1c13fab72d3_f97e3e4a (3).jpg\" type=\"document\" fileEntryId=\"57738525\"><![CDATA[/documents/20143/0/0-02-06-ed9bf22df63aba20b39193e5dc6da9c05359e07a05ffe55d8b08c1c13fab72d3_f97e3e4a+%283%29.jpg/3cfc0e14-9dc3-0693-677f-cde1fbd709bf?t=1596099289993]]></dynamic-content><dynamic-content language-id=\"my_MM\" alt=\"\" name=\"0-02-06-ed9bf22df63aba20b39193e5dc6da9c05359e07a05ffe55d8b08c1c13fab72d3_f97e3e4a.jpg\" title=\"0-02-06-ed9bf22df63aba20b39193e5dc6da9c05359e07a05ffe55d8b08c1c13fab72d3_f97e3e4a.jpg\" type=\"document\" fileEntryId=\"57736654\"><![CDATA[/documents/20143/0/0-02-06-ed9bf22df63aba20b39193e5dc6da9c05359e07a05ffe55d8b08c1c13fab72d3_f97e3e4a.jpg/fb26116d-2420-e87c-5d35-0af52d387dfd?t=1596098597140]]></dynamic-content></dynamic-element><dynamic-element name=\"location\" type=\"text\" index-type=\"keyword\" instance-id=\"brut\"><dynamic-content language-id=\"en_US\"><![CDATA[ပဲခူးတိုင်းဒေသကြီး]]></dynamic-content><dynamic-content language-id=\"my_MM\"><![CDATA[ပဲခူးတိုင်းဒေသကြီး]]></dynamic-content></dynamic-element><dynamic-element name=\"Content\" type=\"text_area\" index-type=\"text\" instance-id=\"orbj\"><dynamic-content language-id=\"en_US\"><![CDATA[<img data-fileentryid=\"57738547\" src=\"/documents/20143/0/view+%281%29.jpg/c985c1d2-437d-9f9a-73c1-b023d45d7888?t=1596099309307\" />]]></dynamic-content><dynamic-content language-id=\"my_MM\"><![CDATA[<img data-fileentryid=\"57738274\" src=\"/documents/20143/0/view.JPG/07fee712-ad55-115b-55bd-e75e1bb28b63?t=1596099148640\" />]]></dynamic-content></dynamic-element></root>";
		//List<Map<String, String>> contentlist  = new DocumentParsing().ParsingImageTextTextArea(content);
		//System.out.println(contentlist);
		//int num = 3;
		//int total =4;
		//int percent = num * 100 / total;
		//System.out.println("Percent___________________" + percent);
		
		 String names1 = "april  thannaing";
	        boolean validation = false ;
	        if(names1.contains(" ")){
	            
	        }else validation = true;
	        System.out.println(validation);
	}
	
	private boolean containsSpecial1(String value) {
		Pattern special = Pattern.compile("[!@#$%&*()+=|<>?{}\\[\\]~]");
		Matcher hasSpecial = special.matcher(value);
		return hasSpecial.find();
	}
}
