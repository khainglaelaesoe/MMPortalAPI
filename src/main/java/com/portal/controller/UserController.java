package com.portal.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

	@Value("${SERVICEURL}")
	private String SERVICEURL;

	@PostMapping("encrypt")
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject encrypt(@RequestParam String originalString) throws Exception {
		JSONObject json = new JSONObject();
		originalString = originalString.replaceAll(" ", "+");
		String encryptedString = AES.encrypt(originalString, secretKey);
		String decryptedString = AES.decrypt(encryptedString, secretKey);
		json.put("encryptedString", encryptedString);
		json.put("decryptedString", decryptedString);
		return json;
	}

	/*
	 * {
	 * 
	 * "oldPassword" : "", "email" :
	 * "4f7d4eca7e7254f2e97252f950931fdf279ceac757455396b7664a1d18c7528c", "phone" :
	 * "", "portrait" : "", "userName" : "67e56c3c6a0d0a7145a37d486437f20e",
	 * "securityQuestion" : "", "securityAnswer" : "", "userId" :
	 * "d153764a379a89888c29b997014875c2" }
	 *
	 * 
	 * { "email" : "nobody.93.vh@gmail.com", "phone" : "0368031140", "portrait" :
	 * "", "userName" : "testpass202000", "securityQuestion" : "abc",
	 * "securityAnswer" : "abc", "name" : "abc" }
	 */

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
		
		String userId = json.get("userId").toString();
		if (!userId.equals(null) && !userId.isEmpty())
			userId = AES.decrypt(userId, secretKey);

		User_ mnpUser = userService.getMNPUserByUserId(userId.toString());
		String email = json.get("email").toString();
		if (!email.equals(null) && !email.isEmpty())
			email = AES.decrypt(email, secretKey);

		String phone = json.get("phone").toString();
		if (!phone.equals(null) && !phone.isEmpty())
			phone = AES.decrypt(phone, secretKey);

		String userName = json.get("userName").toString();
		if (!userName.equals(null) && !userName.isEmpty())
			userName = AES.decrypt(userName, secretKey);

		String portrait = json.get("portrait").toString();
		String phoneNo = userService.getPhoneByUserId(userId.toString());
		JSONObject request = new JSONObject();

		request.put("email", email.isEmpty() ? mnpUser.getEmailaddress() : email);
		request.put("phone", phone.isEmpty() ? phoneNo : phone);
		request.put("portrait", portrait);
		request.put("userName", mnpUser.getScreenname());
		request.put("securityQuestion", mnpUser.getReminderqueryquestion());
		request.put("securityAnswer", mnpUser.getReminderqueryanswer());

		String dbName = (mnpUser.getFirstname() == null ? "" : mnpUser.getFirstname()) + (mnpUser.getMiddlename() == null ? "" : " " + mnpUser.getMiddlename()) + (mnpUser.getLastname() == null ? "" : " " + mnpUser.getLastname());
		request.put("name", userName.isEmpty() ? dbName: userName);

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
			if (j.get("errCode").equals("E13")) 
				resultJson.put("message", "Can not update user! duplicate email address or screen name!");
			else 
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

	private boolean hasEmailFromDB(String email) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("email", email);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(headers);
		logger.info("Request is: " + entityHeader);

		String url = SERVICEURL + "/user/hasEmailFromDB";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<Boolean> response = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, entityHeader, Boolean.class);
		logger.info("response ................" + response.getBody());

		return response.getBody();
	}

	@RequestMapping(value = "register", method = RequestMethod.POST) /* password encrypted */
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject registration(@RequestHeader("Authorization") String encryptedString, @RequestBody JSONObject request) throws Exception {
		JSONObject resultJson = new JSONObject();

		String token = AES.decrypt(request.get("token").toString(), secretKey);
		if (token == null) {
			resultJson.put("status", 0);
			resultJson.put("message", "Token is not valid!");
			return resultJson;
		}

		String email = AES.decrypt(request.get("email").toString(), secretKey);
		String emailFromToken = token.substring(0, token.length() - 6);
		logger.info("Email !!!!!!!!!!!!!!!" + emailFromToken);

		if (!hasEmailFromDB(emailFromToken) || !emailFromToken.equals(email)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Verification Failure!");
			return resultJson;
		}

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

		String encryptedPassword = AES.decrypt(request.get("password").toString(), secretKey);

		JSONObject json = new JSONObject();
		json.put("name", AES.decrypt(request.get("name").toString(), secretKey));
		json.put("displayName", AES.decrypt(request.get("screenname").toString(), secretKey));
		json.put("emailAddress", email);
		json.put("phone", AES.decrypt(request.get("phoneno").toString(), secretKey));
		json.put("password", encryptedPassword);
		json.put("confirmPassword", encryptedPassword);
		json.put("securityQuestion", AES.decrypt(request.get("reminderqueryquestion").toString(), secretKey));
		json.put("securityAnswer", AES.decrypt(request.get("reminderqueryanswer").toString(), secretKey));

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

		String email = AES.decrypt(req.get("email").toString(), secretKey);
		String encryptedPassword = AES.decrypt(req.get("password").toString(), secretKey);
//		String password = AES.decrypt(encryptedPassword, secretKey);
//
//		if (password == null) {
//			resultJson.put("status", 0);
//			resultJson.put("message", "Password is not valid!");
//			return resultJson;
//		}

		HttpHeaders headers = new HttpHeaders();
		JSONObject json = new JSONObject();
		json.put("companyId", "20116");
		json.put("email", email);
		json.put("password", encryptedPassword);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/login";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		JSONObject otherserviceResponse = restTemplate.postForObject(url, entityHeader, JSONObject.class);
		logger.info("Login Response : " + otherserviceResponse);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expireTime = now.plusHours(24);

		if (otherserviceResponse.get("access_token") != null) {
			User_ user = userService.getUserbyemail(email);
			MobileResponse mbresponse = convertoMobileResponse(user);
			resultJson.put("status", "1");
			resultJson.put("user", mbresponse);
			resultJson.put("message", "Login Success!");
			resultJson.put("profilePicture", otherserviceResponse.get("portrait").toString().replace("user", "image/user"));
			resultJson.put("token", otherserviceResponse.get("access_token").toString());
			resultJson.put("expireTime", dtf.format(expireTime));

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
	public JSONObject facebookLogin(@RequestHeader("Authorization") String encryptedString, @RequestHeader("token") String fbtoken, @RequestHeader("email") String email) {
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
		if(email.equals("") || email.equals(null)) {
			response.put("status", 0);
			response.put("message", "email can't be null or empty");
			return response;
		}else
			email = AES.decrypt(email, secretKey);
		
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
		if (otherserviceResponse.getBody().get("access_token") != null) {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime expireTime = now.plusHours(24);
			
			User_ user = userService.getUserbyemail(email);
			MobileResponse mbresponse = convertoMobileResponse(user);
			response.put("status", "1");
			response.put("user", mbresponse);
			response.put("message", "Login Success!");
			response.put("profilePicture", "");
			response.put("token", otherserviceResponse.getBody().get("access_token").toString());
			response.put("expireTime", dtf.format(expireTime));
		} else {
			response.put("status", "0");
			response.put("message", otherserviceResponse.getBody().get("message"));
		}
		return response;
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

		if (code == null) {
			json.put("status", 0);
			json.put("message", "Code is not valid!");
			return json;
		}

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
		String encryptedPassword = req.get("password").toString();
		String password = AES.decrypt(encryptedPassword, secretKey);

		if (code == null) {
			response.put("status", 0);
			response.put("message", "Code is not valid!");
			return response;
		}

		if (password == null) {
			response.put("status", 0);
			response.put("message", "Password is not valid!");
			return response;
		}

		logger.info("encrypted code: !!!!!!!!!!!!!!!!" + encryptedCode);
		logger.info("decrypted code: !!!!!!!!!!!!!!!!" + code);

		logger.info("encryptedPassword: !!!!!!!!!!!!!!!!" + encryptedPassword);
		logger.info("decrypted password: !!!!!!!!!!!!!!!!" + password);

		JSONObject json = new JSONObject();
		json.put("resetToken", req.get("token").toString());
		json.put("code", code);
		json.put("password", password);
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
			response.put("errCode", otherserviceResponse.getBody().get("errCode").toString());
			String message = otherserviceResponse.getBody().get("message").toString();
			if(message.contains("."))
			 response.put("message", message.substring(0,message.indexOf(".")));
			else response.put("message", message);
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

	@RequestMapping(value = "changePassword", method = RequestMethod.POST) /* password encrypted */
	@ResponseBody
	@JsonView(Views.Summary.class)
	public JSONObject changePassword(@RequestHeader("Authorization") String encryptedString, @RequestHeader("token") String token, @RequestBody JSONObject req) {
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
		// oldPassword
		String oldPassword = req.get("oldPassword").toString();
		if (oldPassword.equals("") || oldPassword.equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Invalid email or password!");
			return resultJson;
		} else
			oldPassword = AES.decrypt(oldPassword, secretKey);
		// newPassword1
		String newPassword1 = req.get("newPassword1").toString();
		if (newPassword1.equals("") || newPassword1.equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "New Password cannot be null or empty");
			return resultJson;
		} else
			newPassword1 = AES.decrypt(newPassword1, secretKey);
		// newPassword2
		String newPassword2 = req.get("newPassword2").toString();
		if (newPassword2.equals("") || newPassword2.equals(null)) {
			resultJson.put("status", 0);
			resultJson.put("message", "Confirm New Password cannot be null or empty");
			return resultJson;
		} else
			newPassword2 = AES.decrypt(newPassword2, secretKey);

		HttpHeaders headers = new HttpHeaders();
		JSONObject json = new JSONObject();
		json.put("oldPassword", oldPassword);
		json.put("newPassword1", newPassword1);
		json.put("newPassword2", newPassword2);
		headers.add("Authorization", token);
		HttpEntity<JSONObject> entityHeader = new HttpEntity<>(json, headers);
		logger.info("Request is: " + entityHeader);

		String url = OTHERSERVICEURL.trim() + "auth/change-password";
		logger.info("service url is: " + url);

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		logger.info("calling webservice..." + builder);
		RestTemplate restTemplate = new RestTemplate();
		JSONObject otherserviceResponse = restTemplate.postForObject(url, entityHeader, JSONObject.class);
		logger.info("Login Response : " + otherserviceResponse);

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expireTime = now.plusHours(24);

		if (otherserviceResponse.get("status") != null) {
			resultJson.put("status", "1");
			resultJson.put("message", otherserviceResponse.get("message").toString());

		} else {
			resultJson.put("status", "0");
			resultJson.put("message", otherserviceResponse.get("message").toString());
		}

		return resultJson;
	}
}
