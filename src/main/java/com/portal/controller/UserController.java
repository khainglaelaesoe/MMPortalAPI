package com.portal.controller;

import java.util.function.IntPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.portal.entity.AES;
import com.portal.entity.MobileResponse;
import com.portal.entity.User_;
import com.portal.entity.Views;
import com.portal.service.UserService;

@Controller
@RequestMapping("user")
public class UserController extends AbstractController {

	@Autowired
	private UserService userService;

	private static Logger logger = Logger.getLogger(OrganizationController.class);

	@Value("${OTHERSERVICEURL}")
	private String OTHERSERVICEURL;

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject update(@RequestHeader("token") String token, @RequestBody JSONObject json) throws Exception {
		JSONObject resultJson = new JSONObject();
		Object userId = json.get("userId");
		if (userId == null || userId.toString().isEmpty()) {
			resultJson.put("status", 0);
			resultJson.put("message", "User Id must not be empty.");
			return resultJson;
		}

		User_ mnpUser = userService.getMNPUserByUserId(userId.toString());
		Object oldPasswordObject = json.get("oldPassword");
		if (oldPasswordObject == null || oldPasswordObject.toString().isEmpty()) {
			resultJson.put("status", 0);
			resultJson.put("message", "Please insert old password.");
			return resultJson;
		}

		String oldPassword = oldPasswordObject.toString();
		String newPassword = json.get("newPassword").toString();

		if (oldPassword.equals(newPassword)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Your new password cannot be the same as your old password. Please enter a different password.");
			return resultJson;
		}
		String email = json.get("email").toString();
		String phone = json.get("phone").toString();
		String userName = json.get("userName").toString();
		String portrait = json.get("portrait").toString();

		String phoneNo = userService.getPhoneByUserId(userId.toString());
		JSONObject request = new JSONObject();
		request.put("email", email.isEmpty() ? mnpUser.getEmailaddress() : email);
		request.put("password", newPassword.isEmpty() ? oldPassword : newPassword);
		request.put("phone", phone.isEmpty() ? phoneNo : phone);
		request.put("portrait", portrait);
		request.put("userName", userName.isEmpty() ? mnpUser.getScreenname() : userName);
		request.put("securityQuestion", mnpUser.getReminderqueryquestion());
		request.put("securityAnswer", mnpUser.getReminderqueryanswer());

		String serviceUrl = OTHERSERVICEURL + "user/update-user-info";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);

		HttpEntity<String> entity = new HttpEntity<String>(request.toString(), headers);
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
		User_ user = userService.getUserbyemail(j.get("email").toString());
		// String mbmessage = saveUser(user);////save mbuser
		// resultJson.put("mbmessage", mbmessage);
		resultJson.put("profilePicture", j.get("portrait").toString().replace("user", "image/user"));
		resultJson.put("status", 1);
		resultJson.put("message", "success");
		resultJson.put("phone", request.get("phone"));
		resultJson.put("userName", request.get("userName"));
		resultJson.put("email", request.get("email"));
		return resultJson;
	}

	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject registration(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject request) throws Exception {
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
			return resultJson;
		}

		if (j.get("message").toString().equals("success")) {
			User_ user = userService.getUserbyemail(email);
			// String mbmessage = saveUser(user);////save mbuser
			// resultJson.put("mbmessage", mbmessage);
			resultJson.put("userId", user.getUserid());
			resultJson.put("status", 1);
			resultJson.put("message", j.get("message"));
		}

		return resultJson;
	}

	@RequestMapping(value = "userId", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public User_ getUserId(@RequestBody JSONObject json) {

		return userService.getMNPUserByEmail(json.get("email").toString());
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
			User_ user = userService.getUserbyemail(email);
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", otherserviceResponse.get("portrait").toString());
			response.put("token", otherserviceResponse.get("access_token").toString());
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
		mbresponse.setName(user.getFirstname() + (user.getLastname() == null ? "" : user.getLastname()));
		mbresponse.setPhoneno(user.getPhone());
		return mbresponse;
	}

	@RequestMapping(value = "facebookLogin", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject facebookLogin(@RequestHeader("token") String fbtoken,
			@RequestHeader("email") String email) {

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
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(),
				HttpMethod.GET, entityHeader, JSONObject.class);
		logger.info("Facebook Login Response : " + otherserviceResponse);
		if (otherserviceResponse.getBody().get("access_token") != null) {
			User_ user = userService.getUserbyemail(email);
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", otherserviceResponse.getBody().get("portrait").toString());
			response.put("token", otherserviceResponse.getBody().get("access_token").toString());
		} else {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message"));
			response.put("token", "");
		}
		return response;
	}

//	@RequestMapping(value = "checkQuestion", method = RequestMethod.GET)
//	@ResponseBody
//	@JsonView(Views.Summary.class)
//	public JSONObject checkQuestion(@RequestParam("email") String email) {
//		JSONObject response = new JSONObject();
//		User_ user = userService.getUserbyemail(email);
//		if (user != null) {
//			response.put("questions", user.getReminderqueryquestion() != null ? user.getReminderqueryquestion() : "");
//			response.put("iosQuestions", user.getReminderqueryquestion() != null ? user.getReminderqueryquestion() : "");
//			response.put("answer", user.getReminderqueryanswer() != null ? user.getReminderqueryanswer() : "");
//			response.put("status", "1");
//			response.put("message", "Success!");
//		} else {
//			response.put("status", "0");
//			response.put("message", "Email Not Found!");
//			return response;
//		}
//		return response;
//	}
	// 1
		@RequestMapping(value = "resetpassword1", method = RequestMethod.GET)
		@ResponseBody
		@JsonView(Views.Summary.class)
		public JSONObject resetpassword1(@RequestParam("email") String email) {
			JSONObject response = new JSONObject();
			HttpHeaders headers = new HttpHeaders();
			HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
			logger.info("Request is: " + entityHeader);

			String url = OTHERSERVICEURL + "auth/reset-password";
			logger.info("service url is: " + url);

			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("email", email);
			logger.info("calling webservice..." + builder);
			RestTemplate restTemplate = new RestTemplate();
			HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(),
					HttpMethod.GET, entityHeader, JSONObject.class);
			if (otherserviceResponse.getBody().get("errCode") != null) {
				response.put("status", "0");
				response.put("message", otherserviceResponse.getBody().get("message").toString());
				return response;

			}
			response.put("status", "1");
			response.put("message", "Success!");
			response.put("securityQuestion", otherserviceResponse.getBody().get("securityQuestion").toString());

			return response;
		}

	// 2
	@RequestMapping(value = "resetpassword2", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	private JSONObject resetpassword2(@RequestBody JSONObject req) {
		JSONObject response = new JSONObject();
		JSONObject json = new JSONObject();
		json.put("email", req.get("email").toString());
		json.put("securityAnswer", req.get("securityAnswer").toString());
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);
		String url = OTHERSERVICEURL + "auth/request-reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(),
				HttpMethod.POST, entityHeader, JSONObject.class);
		if (otherserviceResponse.getBody().get("resetToken") != null) {
			response.put("status", "1");
			response.put("message", "Success!");
			response.put("token", otherserviceResponse.getBody().get("resetToken").toString());
			return response;
		}
		response.put("status", "0");
		response.put("message", otherserviceResponse.getBody().get("message").toString());

		return response;
	}

	@RequestMapping(value = "resetpassword3", method = RequestMethod.POST)
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

		String url = OTHERSERVICEURL + "auth/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(),
				HttpMethod.POST, entityHeader, JSONObject.class);
		if (otherserviceResponse.getBody().get("errCode") != null) {
			if (otherserviceResponse.getBody().get("errCode").equals("E20")
					|| otherserviceResponse.getBody().get("errCode").equals("E21")) {
				response.put("status", "0");
				response.put("message", otherserviceResponse.getBody().get("message").toString());
				return response;
			}
			response.put("status", "1");
			response.put("message", "Success!");
		}
		return response;
	}

	@RequestMapping(value = "resetpassword4", method = RequestMethod.POST)
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

		String url = OTHERSERVICEURL + "auth/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(),
				HttpMethod.POST, entityHeader, JSONObject.class);
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

	@RequestMapping(value = "appleidlogin", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject appleLogin(@RequestBody JSONObject json) throws Exception {
		JSONObject resultJson = new JSONObject();

		Object email = json.get("email");
		if (email == null || email.toString().isEmpty()) {
			resultJson.put("message", "Email must not be empty!");
			resultJson.put("status", "0");
			return resultJson;
		}

		// if new
		// generate password
		// call register
		// save password

//		mobileuser mobileuser = mobileUserService.getUserByEmail(email.toString().trim(), facebookid.toString());
//		if (mobileuser != null) {
//			resultJson.put("user", mobileuser);
//			resultJson.put("message", "Exiting email");
//			resultJson.put("status", "2");
//			resultJson.put("profilePicture", mobileuser.getProfilePicture() == null || mobileuser.getProfilePicture().isEmpty() ? "" : IMAGEURL + mobileuser.getProfilePicture());
//			return resultJson;
//		} else {
//			mobileuser user = parsefacebookUser(json);
//			mobileUserService.saveUser(user);
//			resultJson.put("userId", user.getUserid());
//			resultJson.put("message", "New email");
//			resultJson.put("status", "1");
//			resultJson.put("profilePicture", "");
//		}
		return resultJson;
	}

	@RequestMapping(value = "ValidateRegistration", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject ValidateRegistration(@RequestBody JSONObject request) throws Exception {
		JSONObject resultJson = new JSONObject();
		if (request.get("name").toString().equals("") || request.get("name").toString().equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! Name cannot be null or empty");
			return resultJson;
		}
		if (request.get("screenname").toString().equals("") || request.get("screenname").toString().equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user!Screen Name cannot be null or empty");
			return resultJson;
		}

		if (request.get("email").toString().equals("") || request.get("email").toString().equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! emailAddress cannot be null or empty");
			return resultJson;
		}

		if (request.get("password").toString().equals("") || request.get("password").toString().equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! password cannot be null or empty");
			return resultJson;
		}

		User_ user = userService.getUserbyemail(request.get("email").toString());
		if (user != null) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! This email is already registered!");
			return resultJson;
		}

		User_ user1 = userService.getScreenName(request.get("screenname").toString());
		if (user1 != null) {
			resultJson.put("status", 0);
			resultJson.put("message","Can not create user! Screen name " + request.get("screenname").toString() + " must not be duplicate but is already used.");
			return resultJson;
		}
		String password = request.get("password").toString();
		if (password.length() < 8) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! Password for user must be at least 8 characters");
			return resultJson;
		} else {
			
			if (!containsUpperCase(password)) {
				resultJson.put("status", 0);
				resultJson.put("message","Can not create user! Password must have at least 1 uppercase characters");
				return resultJson;
			}
			if (!containsLowerCase(password)) {
				resultJson.put("status", 0);
				resultJson.put("message","Can not create user! Password must have at least 1 lowercase characters");
				return resultJson;
			}
			
			if (!containsNumber(password)) {
				resultJson.put("status", 0);
				resultJson.put("message","Can not create user! Password must have at least 1 numbers");
				return resultJson;
			}
			if (!containsSpecial(password)) {
				resultJson.put("status", 0);
				resultJson.put("message","Can not create user! Password must have at least 1 specital characters");
				return resultJson;
			}
		}
		resultJson.put("status", 1);
		resultJson.put("message","Success");
		return resultJson;
	}
	@RequestMapping(value = "ValidateEmail", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject ValidateEmail(@RequestBody JSONObject request) throws Exception {
		JSONObject resultJson = new JSONObject();

		if (request.get("email").toString().equals("") || request.get("email").toString().equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! emailAddress cannot be null or empty");
			return resultJson;
		}

		User_ user = userService.getUserbyemail(request.get("email").toString());
		if (user != null) {
			resultJson.put("status", 0);
			resultJson.put("message", "Can not create user! This email is already registered!");
			return resultJson;
		}

		resultJson.put("status", 1);
		resultJson.put("message","Success");
		return resultJson;
	}
	private boolean contains(String value, IntPredicate predicate) {
	    return value.chars().anyMatch(predicate);
	}
	private boolean containsLowerCase(String value) {
	    return contains(value, i -> Character.isLetter(i) && Character.isLowerCase(i));
	}
	private boolean containsUpperCase(String value) {
	    return contains(value, i -> Character.isLetter(i) && Character.isUpperCase(i));
	}
	private boolean containsNumber(String value) {
	    return contains(value, Character::isDigit);
	}
	
	private boolean containsSpecial(String value) {
		Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
		Matcher hasSpecial = special.matcher(value);
		return hasSpecial.find();
	}
}
