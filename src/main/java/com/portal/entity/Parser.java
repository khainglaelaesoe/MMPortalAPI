package com.portal.entity;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Parser {
	private static Logger logger = Logger.getLogger(Parser.class);

	public static JSONObject parseJSon(String input) {
		JSONObject json = null;
		try {
			json = (JSONObject) new JSONParser().parse(decode(input));
		} catch (Exception e) {
			logger.error("Error :", e);
		}
		return json;
	}
	
	private static String decode(String encString){
		StringTokenizer token = new StringTokenizer(encString, ",");
		StringBuffer buffer = new StringBuffer();
		while (token.hasMoreTokens()) {
			buffer.append((char) Integer.parseInt(token.nextToken()));
		}
		return buffer.toString();
	}

}
