package com.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "User_")
public class User_ {

	@Id
	@JsonView(Views.Thin.class)
	@Column(name = "userid", unique = true, nullable = false)
	private long userid;

	@JsonView(Views.Thin.class)
	@Column(name = "emailaddress")
	private String emailaddress;

	@JsonView(Views.Thin.class)
	@Column(name = "companyid")
	private String companyid;

	@JsonView(Views.Thin.class)
	@Column(name = "createdate")
	private String createdate;

	@JsonView(Views.Thin.class)
	@Column(name = "modifieddate")
	private String modifieddate;

	@JsonView(Views.Thin.class)
	@Column(name = "password_")
	private String password_;

	@JsonView(Views.Thin.class)
	@Column(name = "passwordreset")
	private String passwordreset;

	@JsonView(Views.Thin.class)
	@Column(name = "screenname")
	private String screenname;

	@JsonView(Views.Thin.class)
	@Column(name = "jobtitle")
	private String jobtitle;

	@JsonView(Views.Thin.class)
	@Column(name = "firstname")
	private String firstname;

	@JsonView(Views.Thin.class)
	@Column(name = "middlename")
	private String middlename;

	@JsonView(Views.Thin.class)
	@Column(name = "lastname")
	private String lastname;

	@JsonView(Views.Thin.class)
	@Column(name = "facebookid")
	private long facebookid;

	@JsonView(Views.Thin.class)
	@Column(name = "reminderqueryquestion")
	private String reminderqueryquestion;

	@JsonView(Views.Thin.class)
	@Column(name = "reminderqueryanswer")
	private String reminderqueryanswer;
	
	@JsonView(Views.Thin.class)
	@Transient
	private String phone;
	
	@JsonIgnore
	@Transient
	private String name;

	
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getEmailaddress() {
		return emailaddress;
	}

	public void setEmailaddress(String emailaddress) {
		this.emailaddress = emailaddress;
	}

	public String getCompanyid() {
		return companyid;
	}

	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

	public String getCreatedate() {
		return createdate;
	}

	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}

	public String getModifieddate() {
		return modifieddate;
	}

	public void setModifieddate(String modifieddate) {
		this.modifieddate = modifieddate;
	}

	public String getPassword_() {
		return password_;
	}

	public void setPassword_(String password_) {
		this.password_ = password_;
	}

	public String getPasswordreset() {
		return passwordreset;
	}

	public void setPasswordreset(String passwordreset) {
		this.passwordreset = passwordreset;
	}

	public String getScreenname() {
		return screenname;
	}

	public void setScreenname(String screenname) {
		this.screenname = screenname;
	}

	public String getJobtitle() {
		return jobtitle;
	}

	public void setJobtitle(String jobtitle) {
		this.jobtitle = jobtitle;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getMiddlename() {
		return middlename;
	}

	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public long getFacebookid() {
		return facebookid;
	}

	public void setFacebookid(long facebookid) {
		this.facebookid = facebookid;
	}

	public String getReminderqueryquestion() {
		return reminderqueryquestion;
	}

	public void setReminderqueryquestion(String reminderqueryquestion) {
		this.reminderqueryquestion = reminderqueryquestion;
	}

	public String getReminderqueryanswer() {
		return reminderqueryanswer;
	}

	public void setReminderqueryanswer(String reminderqueryanswer) {
		this.reminderqueryanswer = reminderqueryanswer;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
