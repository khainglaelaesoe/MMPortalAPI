package com.portal.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.portal.entity.Organization_;
import com.portal.entity.TopicEngName;
import com.portal.entity.TopicMyanmarName;
import com.portal.entity.Views;

@Controller
@RequestMapping("topic")
public class TopicController extends AbstractController {

	private static Logger logger = Logger.getLogger(TopicController.class);

	@RequestMapping(value = "names", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public JSONObject getServiceName() {
		JSONObject json = new JSONObject();
		List<Organization_> organizationList = new ArrayList<Organization_>();
		for (TopicMyanmarName name : TopicMyanmarName.values()) {
			Organization_ org = new Organization_();
			org.setMyanmarName(name.getValue());
			org.setEngName(TopicEngName.valueOf(name.toString()).getValue());
			org.setKey(name.toString());
			organizationList.add(org);
		}
		json.put("topicList", organizationList);
		return json;
	}

	@RequestMapping(value = "images", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(Views.Thin.class)
	public void getImageByTopic(HttpServletResponse response) throws IOException {
		try {
			String image = "C:\\Users\\DELL\\Documents\\Images\\health.jpeg";
			File file = new File(image);
			InputStream fileInputStream;
			fileInputStream = new FileInputStream(file);
			IOUtils.copy(fileInputStream, response.getOutputStream());
		} catch (FileNotFoundException e) {
			logger.info("Error: " + e);
		}

//		JSONObject json = new JSONObject();
//		List<Organization> organizationList = new ArrayList<Organization>();
//		for (TopicImage topic : TopicImage.values()) {
//			Organization org = new Organization();
//			org.setKey(topic.toString());
//			org.setEngName(TopicEngName.valueOf(topic.toString()).getValue());
//			org.setImage(topic.getValue());
//			organizationList.add(org);
//		}
//		json.put("images", organizationList);
//		return json;
	}

}
