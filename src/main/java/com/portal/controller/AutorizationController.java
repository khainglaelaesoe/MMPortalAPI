package com.portal.controller;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/auth")
public class AutorizationController {

	/*
	 * @RequestMapping(value = "/", method = RequestMethod.GET, produces
	 * ="application/json")
	 * 
	 * @ResponseBody public String home() { return "Welcome home!"; }
	 */

	@GetMapping(path = "/", produces = "application/json")
	public JSONObject getAuthString() {
		JSONObject json = new JSONObject();
		json.put("message", "success");
		return json;
	}
}
