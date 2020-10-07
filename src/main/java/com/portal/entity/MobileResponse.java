package com.portal.entity;

import com.fasterxml.jackson.annotation.JsonView;

public class MobileResponse {
	@JsonView(Views.Thin.class)
	private long userid = 0;
	
	@JsonView(Views.Thin.class)
	private String screenname= "";
	
	@JsonView(Views.Thin.class)
	private String emailaddress= "";
	
	@JsonView(Views.Thin.class)
	private String name= "";
	
	@JsonView(Views.Thin.class)
	private String profilePicture= "";
	
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public String getScreenname() {
		return screenname;
	}
	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}
	public String getEmailaddress() {
		return emailaddress;
	}
	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProfilePicture() {
		return profilePicture;
	}
	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}
}
