package com.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "User_")
public class User_ {

	@Id
	@JsonIgnore
	@Column(name = "userid", unique = true, nullable = false)
	private long userid;

	@JsonIgnore
	@Column(name = "emailaddress")
	private String emailaddress;

	@JsonIgnore
	@Column(name = "companyid")
	private String companyid;

	@JsonIgnore
	@Column(name = "createdate")
	private String createdate;

	@JsonIgnore
	@Column(name = "modifieddate")
	private String modifieddate;

	@JsonIgnore
	@Column(name = "password_")
	private String password_;

	@JsonIgnore
	@Column(name = "passwordreset")
	private String passwordreset;

	@JsonIgnore
	@Column(name = "screenname")
	private String screenname;

	@JsonIgnore
	@Column(name = "jobtitle")
	private String jobtitle;

	@JsonIgnore
	@Column(name = "firstname")
	private String firstname;

	@JsonIgnore
	@Column(name = "middlename")
	private String middlename;

	@JsonIgnore
	@Column(name = "lastname")
	private String lastname;

	@JsonIgnore
	@Column(name = "facebookid")
	private long facebookid;

	@JsonIgnore
	@Column(name = "reminderqueryquestion")
	private String reminderqueryquestion;

	@JsonIgnore
	@Column(name = "reminderqueryanswer")
	private String reminderqueryanswer;
	
	@JsonIgnore
	@Transient
	private String phone;

	
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

}
