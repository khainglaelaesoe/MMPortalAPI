package com.portal.controller;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject update(@RequestHeader("Authorization") String token, @RequestBody JSONObject json) throws Exception {
		JSONObject resultJson = new JSONObject();
		String serviceUrl = OTHERSERVICEURL + "user/update-user-info";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);

		HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);
		RestTemplate restTemplate = new RestTemplate();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		factory.setHttpClient(httpClient);
		restTemplate.setRequestFactory(factory);

		ResponseEntity<String> response = restTemplate.exchange(serviceUrl, HttpMethod.POST, entity, String.class);
		JSONParser parser = new JSONParser();
		JSONObject j = (JSONObject) parser.parse(response.getBody());

		if (j.get("errCode") != null) {
			resultJson.put("status", 0);
			resultJson.put("message", j.get("message"));
			return resultJson;
		}

		resultJson.put("profilePicture", j.get("portrait").toString().replace("user", "image/user"));
		resultJson.put("status", 1);
		resultJson.put("message", "success");
		return resultJson;
	}

	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject registration(@RequestBody JSONObject request) throws Exception {
		JSONObject resultJson = new JSONObject();

		String serviceUrl = OTHERSERVICEURL + "auth/register";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String email = request.get("email").toString();

		JSONObject json = new JSONObject();
		json.put("name", request.get("name").toString());
		json.put("displayName", request.get("screenname").toString());
		json.put("emailAddress", email);
		json.put("phone", request.get("phoneno").toString());
		json.put("password", request.get("password").toString());
		json.put("confirmPassword", request.get("password").toString());
		json.put("securityQuestion", request.get("reminderqueryquestion").toString());
		json.put("securityAnswer", request.get("reminderqueryanswer").toString());

		HttpEntity<String> entity = new HttpEntity<String>(json.toString(), headers);
		RestTemplate restTemplate = new RestTemplate();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		HttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		factory.setHttpClient(httpClient);
		restTemplate.setRequestFactory(factory);

		ResponseEntity<String> response = restTemplate.exchange(serviceUrl, HttpMethod.POST, entity, String.class);
		JSONParser parser = new JSONParser();
		JSONObject j = (JSONObject) parser.parse(response.getBody());

		if (j.get("errCode") != null) {
			resultJson.put("status", 0);
			resultJson.put("message", j.get("message"));
		}

		Long userId = userservice.getIdByEmail(email);
		if (j.get("message").toString().equals("success")) {
			resultJson.put("userId", userId);
			resultJson.put("status", 1);
			resultJson.put("message", j.get("message"));
		}

		return resultJson;
	}

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
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "auth/login";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		JSONObject otherserviceResponse = restTemplate.postForObject(url, entityHeader, JSONObject.class);
		logger.info("Login Response : " + otherserviceResponse);
		if (otherserviceResponse.get("access_token") != null) {
			User_ user = userservice.getUserbyemail(email);
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", "");
		} else {
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
		mbresponse.setName(user.getFirstname() + user.getLastname() == null ? "" : user.getLastname());
		return mbresponse;
	}

	@RequestMapping(value = "facebookLogin", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject facebookLogin(@RequestHeader("Authorization") String fbtoken, @RequestHeader("facebookID") String facebookID) {

		JSONObject response = new JSONObject();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", fbtoken);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "auth/login-with-facebook";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, JSONObject.class);
		logger.info("Facebook Login Response : " + otherserviceResponse);
		if (otherserviceResponse.getBody().get("errCode") != "") {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message"));
		} else {
			User_ user = userservice.getUserbyfacebookID(facebookID);
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", "");
		}
		return response;
	}

	@RequestMapping(value = "checkQuestion", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject checkQuestion(@RequestParam("email") String email) {
		JSONObject response = new JSONObject();
		User_ user = userservice.getUserbyemail("aprilthannaing1995@gmail.com");
		if (user != null) {

			response.put("questions", user.getReminderqueryquestion() != null ? user.getReminderqueryquestion() : "");
			response.put("iosQuestions", user.getReminderqueryquestion() != null ? user.getReminderqueryquestion() : "");
			response.put("answer", user.getReminderqueryanswer() != null ? user.getReminderqueryanswer() : "");
			response.put("status", "1");
			response.put("message", "Success!");
		} else {
			response.put("status", "0");
			response.put("message", "Email Not Found!");
			return response;
		}
		return response;
	}

	@RequestMapping(value = "resetpassword", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject checkemail(@RequestParam("email") String email) {
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
		if (otherserviceResponse.getBody().get("resetToken") != null) {
			resToken = otherserviceResponse.getBody().get("resetToken").toString();
			response.put("status", "1");
			response.put("message", "Success!");
			response.put("token", resToken);
		} else {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message").toString());
			return response;
		}

		return response;
	}

	@RequestMapping(value = "resetpasswordCode", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	private JSONObject resetpasswordCode(@RequestBody JSONObject req) {
		JSONObject response = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("resetToken", req.get("token").toString());
		json.put("code", req.get("code").toString());
		json.put("password", "");
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, JSONObject.class);
		if (otherserviceResponse.getBody().get("errCode") != null) {
			if (otherserviceResponse.getBody().get("errCode").equals("E20") || otherserviceResponse.getBody().get("errCode").equals("E21")) {
				response.put("status", "0");
				response.put("message", otherserviceResponse.getBody().get("message").toString());
				return response;
			}
			response.put("status", "1");
			response.put("message", "Success!");
		}
		return response;
	}

	@RequestMapping(value = "resetpassword", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	private JSONObject resetpasswordbyToken(@RequestBody JSONObject req) {
		JSONObject response = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("resetToken", req.get("token").toString());
		json.put("code", req.get("code").toString());
		json.put("password", req.get("password").toString());
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL + "/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, JSONObject.class);
		if (otherserviceResponse.getBody().get("errCode") != null) {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message").toString());
		} else {
			response.put("status", "1");
			response.put("message", otherserviceResponse.getBody().get("message").toString());
			return response;
		}
		return response;
	}

}
