package com.portal.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.MobileResponse;
import com.portal.entity.User_;
import com.portal.entity.Views;
import com.portal.service.UserService;

@Controller
@RequestMapping("user")
public class UserController {
	
	@Autowired 
	private UserService userservice;
	private static Logger logger = Logger.getLogger(OrganizationController.class);
	
	@Value("${OTHERSERVICEURL}")
	private String OTHERSERVICEURL;

	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject login(@RequestBody JSONObject req) {
		String email = req.get("email").toString();
		String password = req.get("password").toString();
		JSONObject response = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		JSONObject json = new JSONObject();
		json.put("companyId", "20116");
		json.put("email", email);
		json.put("password", password);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json,headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "/login";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		JSONObject otherserviceResponse = restTemplate.postForObject(url, entityHeader, JSONObject.class);
		logger.info("Login Response : " + otherserviceResponse);
		if(otherserviceResponse.get("access_token") != null) {
			User_ user = userservice.getUserbyemail("aprilthannaing1995@gmail.com");
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", "");
		}else {
			response.put("status", "0");
			response.put("message", "Your email or password was incorrect. please try again");
		}
			
		return response;
	}
	private MobileResponse convertoMobileResponse(User_ user) {
		MobileResponse mbresponse = new MobileResponse();
		mbresponse.setUserid(user.getUserid());
		mbresponse.setScreenname(user.getScreenname());
		mbresponse.setEmailaddress(user.getEmailaddress());
		mbresponse.setName(user.getFirstname() + user.getLastname());
		return mbresponse;
	}
	@RequestMapping(value = "facebookLogin", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject facebookLogin(@RequestHeader("Authorization") String fbtoken){
		JSONObject response = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", fbtoken);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "/login-with-facebook";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, JSONObject.class);
		logger.info("Facebook Login Response : " + otherserviceResponse);
		if(otherserviceResponse.getBody().get("errCode") != "") {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message"));
		}else {
			User_ user = userservice.getUserbyemail("aprilthannaing1995@gmail.com");
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", "");
		}
		return response;
	}
	
	@RequestMapping(value = "checkemail", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject checkemail(@RequestParam("email") String email){
		String resToken = "";
		JSONObject response = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("email", email);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, JSONObject.class);
		if(otherserviceResponse.getBody().get("resetToken") != null) {
			resToken =  otherserviceResponse.getBody().get("resetToken").toString();
			User_ user = userservice.getUserbyemail("aprilthannaing1995@gmail.com");
			response.put("questions", user.getReminderqueryquestion() != null ? user.getReminderqueryquestion() : "");
			response.put("iosQuestions", user.getReminderqueryquestion() != null ? user.getReminderqueryquestion() : "");
			response.put("answer", user.getReminderqueryanswer() != null ? user.getReminderqueryanswer() : "");
			response.put("status", "1");
			response.put("message", "Success!");
			response.put("userid", user.getUserid());
			response.put("userid", user.getUserid());
		}else {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message").toString());
			return response;
		}
		
		return response;
	}
	
	@RequestMapping(value = "resetpassword", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	private JSONObject resetpasswordbyToken(String resToken,String password) {
		JSONObject response = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.add("resetToken", resToken);
		headers.add("code", "871616");
		headers.add("password", password);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, JSONObject.class);
		if(otherserviceResponse.getBody().get("resetToken") != null) {
			resToken =  otherserviceResponse.getBody().get("resetToken").toString();
		}else {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message").toString());
			return response;
		}
		return response;
	}
}
