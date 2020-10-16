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
import org.springframework.web.bind.annotation.PostMapping;
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

	@PostMapping("encrypt")
	public JSONObject encrypt(@RequestParam String encryptedString) throws Exception {
		JSONObject json = new JSONObject();
		encryptedString = encryptedString.replaceAll(" ", "+");
		String decryptedString = AES.decrypt(encryptedString, secretKey);
		json.put("encryptedString", encryptedString);
		json.put("decryptedString", decryptedString);
		if (isAuthorize(decryptedString))
			json.put("Authorization", "Authorization Success.");
		else
			json.put("Authorization", "Authorization Failure.");
		return json;
	}

	@RequestMapping(value = "update", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject update(@RequestHeader("Authorization") String encryptedString, @RequestHeader("token") String token, @RequestBody JSONObject json) throws Exception {
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
		String name = json.get("name").toString();

		String phoneNo = userService.getPhoneByUserId(userId.toString());
		JSONObject request = new JSONObject();
		request.put("email", email.isEmpty() ? mnpUser.getEmailaddress() : email);
		request.put("password", newPassword.isEmpty() ? oldPassword : newPassword);
		request.put("phone", phone.isEmpty() ? phoneNo : phone);
		request.put("portrait", portrait);
		request.put("userName", userName.isEmpty() ? mnpUser.getScreenname() : userName);
		request.put("securityQuestion", mnpUser.getReminderqueryquestion());
		request.put("securityAnswer", mnpUser.getReminderqueryanswer());

		String dbName = (mnpUser.getFirstname() == null ? "" : mnpUser.getFirstname()) + (mnpUser.getMiddlename() == null ? "" : " " + mnpUser.getMiddlename()) + (mnpUser.getLastname() == null ? "" : mnpUser.getLastname());
		request.put("name", name.isEmpty() ? dbName : name);

		String serviceUrl = OTHERSERVICEURL.trim() + "user/update-user-info";
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
		resultJson.put("profilePicture", j.get("portrait").toString().replace("user", "image/user"));
		resultJson.put("name", j.get("name").toString());
		resultJson.put("status", 1);
		resultJson.put("message", "success");
		resultJson.put("phone", request.get("phone"));
		resultJson.put("userName", request.get("userName"));
		resultJson.put("email", request.get("email"));
		return resultJson;
	}

	@RequestMapping(value = "register", method = RequestMethod.POST) /* password encrypted */
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

		String serviceUrl = OTHERSERVICEURL.trim() + "auth/register";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String email = request.get("email").toString();
		String encryptedPassword = request.get("password").toString();
		String password = AES.decrypt(encryptedPassword, secretKey);

		JSONObject json = new JSONObject();
		json.put("name", request.get("name").toString());
		json.put("displayName", request.get("screenname").toString());
		json.put("emailAddress", email);
		json.put("phone", request.get("phoneno").toString());
		json.put("password", password);
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
			resultJson.put("userId", user != null ? user.getUserid() : "");
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

	@RequestMapping(value = "login", method = RequestMethod.POST) /* password encrypted */
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject login(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject req) {
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

		String email = req.get("email").toString();
		String encryptedPassword = req.get("password").toString();
		String password = AES.decrypt(encryptedPassword, secretKey);

		HttpHeaders headers = new HttpHeaders();
		JSONObject json = new JSONObject();
		json.put("companyId", "20116");
		json.put("email", email);
		json.put("password", password);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/login";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		JSONObject otherserviceResponse = restTemplate.postForObject(url, entityHeader, JSONObject.class);
		logger.info("Login Response : " + otherserviceResponse);
		if (otherserviceResponse.get("access_token") != null) {
			User_ user = userService.getUserbyemail(email);
			MobileResponse mbresponse = convertoMobileResponse(user);
			resultJson.put("status", "1");
			resultJson.put("user", mbresponse);
			resultJson.put("message", "Login Success!");
			resultJson.put("profilePicture", otherserviceResponse.get("portrait").toString());
			resultJson.put("token", otherserviceResponse.get("access_token").toString());
		} else {
			resultJson.put("status", "0");
			resultJson.put("message", "Your email or password was incorrect. please try again");
		}

		return resultJson;
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
	public JSONObject facebookLogin(@RequestHeader("Authorization") String encryptedString, @RequestHeader("token") String fbtoken, @RequestHeader("facebookID") String facebookID) {
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

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", fbtoken);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/login-with-facebook";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, JSONObject.class);
		logger.info("Facebook Login Response : " + otherserviceResponse);

		if (otherserviceResponse.getBody().get("errCode") != "") {
			resultJson.put("status", "0");
			resultJson.put("message", otherserviceResponse.getBody().get("message"));
		} else {
			User_ user = userService.getUserbyfacebookID(facebookID);
			MobileResponse mbresponse = convertoMobileResponse(user);
			resultJson.put("status", "1");
			resultJson.put("user", mbresponse);
			resultJson.put("message", "Login Success!");
			resultJson.put("profilePicture", "");
		}
		return resultJson;
	}

	// 1
	@RequestMapping(value = "resetpassword1", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject resetpassword1(@RequestHeader("Authorization") String encryptedString, @RequestParam("email") String email) {
		JSONObject response = new JSONObject();

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				response.put("status", 0);
				response.put("message", "Authorization failure!");
				return response;
			}
		} catch (Exception e) {
			response.put("status", 0);
			response.put("message", "Authorization failure!");
			return response;
		}

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("email", email);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, JSONObject.class);
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
	private JSONObject resetpassword2(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject req) {
		JSONObject response = new JSONObject();

		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				response.put("status", 0);
				response.put("message", "Authorization failure!");
				return response;
			}
		} catch (Exception e) {
			response.put("status", 0);
			response.put("message", "Authorization failure!");
			return response;
		}

		JSONObject json = new JSONObject();
		json.put("email", req.get("email").toString());
		json.put("securityAnswer", req.get("securityAnswer").toString());
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);
		String url = OTHERSERVICEURL.trim() + "auth/request-reset-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<JSONObject> otherserviceResponse = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entityHeader, JSONObject.class);
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
	private JSONObject resetpasswordCode(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject req) {
		JSONObject response = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				response.put("status", 0);
				response.put("message", "Authorization failure!");
				return response;
			}
		} catch (Exception e) {
			response.put("status", 0);
			response.put("message", "Authorization failure!");
			return response;
		}

		JSONObject json = new JSONObject();
		String encryptedCode = req.get("code").toString();
		String code = AES.decrypt(encryptedCode, secretKey);

		json.put("resetToken", req.get("token").toString());
		json.put("code", code);
		json.put("password", "");
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/reset-password";
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

	@RequestMapping(value = "resetpassword4", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	private JSONObject resetpasswordbyToken(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject req) {
		JSONObject response = new JSONObject();
		try {
			String decryptedString = AES.decrypt(encryptedString, secretKey);
			if (!isAuthorize(decryptedString)) {
				response.put("status", 0);
				response.put("message", "Authorization failure!");
				return response;
			}
		} catch (Exception e) {
			response.put("status", 0);
			response.put("message", "Authorization failure!");
			return response;
		}

		String encryptedCode = req.get("code").toString();
		String code = AES.decrypt(encryptedCode, secretKey);

		JSONObject json = new JSONObject();
		json.put("resetToken", req.get("token").toString());
		json.put("code", code);
		json.put("password", req.get("password").toString());
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/reset-password";
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

	@RequestMapping(value = "ValidateRegistration", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject ValidateRegistration(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject request) throws Exception {
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
			resultJson.put("message", "Can not create user! Screen name " + request.get("screenname").toString() + " must not be duplicate but is already used.");
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
				resultJson.put("message", "Can not create user! Password must have at least 1 uppercase characters");
				return resultJson;
			}
			if (!containsLowerCase(password)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Can not create user! Password must have at least 1 lowercase characters");
				return resultJson;
			}

			if (!containsNumber(password)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Can not create user! Password must have at least 1 numbers");
				return resultJson;
			}
			if (!containsSpecial(password)) {
				resultJson.put("status", 0);
				resultJson.put("message", "Can not create user! Password must have at least 1 specital characters");
				return resultJson;
			}
		}
		resultJson.put("status", 1);
		resultJson.put("message", "Success");
		return resultJson;
	}

	@RequestMapping(value = "ValidateEmail", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject ValidateEmail(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject request) throws Exception {
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
		resultJson.put("message", "Success");
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
