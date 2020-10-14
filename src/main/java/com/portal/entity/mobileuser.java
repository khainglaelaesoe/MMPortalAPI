package com.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "mobileuser")
public class mobileuser extends AbstractEntity implements Serializable {

	@Id
	@JsonView(Views.Thin.class)
	@Column(name = "userid", unique = true, nullable = false)
	private long userid;
	
	@JsonView(Views.Thin.class)
	@Column(name = "webuserid")
	private long webuserid;

	@JsonView(Views.Thin.class)
	@Column(name = "createdate")
	private Date createdate;

	@JsonView(Views.Thin.class)
	@Column(name = "modifieddate")
	private Date modifieddate;

	@JsonView(Views.Thin.class)
	@Column(name = "passwordmodifieddate")
	private Date passwordmodifieddate;

	@JsonView(Views.Thin.class)
	@Column(name = "screenname", nullable = false)
	private String screenname;

	@JsonView(Views.Thin.class)
	@Column(name = "emailaddress")
	private String emailaddress;

	@JsonView(Views.Thin.class)
	@Column(name = "facebookid")
	private long facebookid;

	@JsonView(Views.Thin.class)
	@Column(name = "googleuserid")
	private String googleuserid;

	@JsonView(Views.Thin.class)
	@Column(name = "name")
	private String name;

	@JsonView(Views.Thin.class)
	@Column(name = "jobtitle")
	private String jobtitle;

	@JsonView(Views.Thin.class)
	@Column(name = "logindate")
	private Date logindate;

	@JsonView(Views.Thin.class)
	@Column(name = "androidid")
	private String androidid;

	@JsonView(Views.Thin.class)
	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private EntityStatus status;

	@JsonView(Views.Thin.class)
	@Column(name = "phoneno")
	private String phoneno;

	@Transient
	private String newpassword1;

	@Transient
	private String newpassword2;

	@JsonIgnore
	@Column(name = "passwordreset")
	private int passwordreset;


	@Column(name = "password")
	private String password;

	@JsonIgnore
	@Column(name = "reminderqueryquestion")
	private String reminderqueryquestion;

	@JsonIgnore
	@Column(name = "reminderqueryanswer")
	private String reminderqueryanswer;

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public Date getCreatedate() {
		return createdate;
	}

	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}

	public Date getModifieddate() {
		return modifieddate;
	}

	public void setModifieddate(Date modifieddate) {
		this.modifieddate = modifieddate;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getPasswordmodifieddate() {
		return passwordmodifieddate;
	}

	public void setPasswordmodifieddate(Date passwordmodifieddate) {
		this.passwordmodifieddate = passwordmodifieddate;
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

	public long getFacebookid() {
		return facebookid;
	}

	public void setFacebookid(long facebookid) {
		this.facebookid = facebookid;
	}

	public String getGoogleuserid() {
		return googleuserid;
	}

	public void setGoogleuserid(String googleuserid) {
		this.googleuserid = googleuserid;
	}

	public String getJobtitle() {
		return jobtitle;
	}

	public void setJobtitle(String jobtitle) {
		this.jobtitle = jobtitle;
	}

	public Date getLogindate() {
		return logindate;
	}

	public void setLogindate(Date logindate) {
		this.logindate = logindate;
	}

	public String getAndroidid() {
		return androidid;
	}

	public void setAndroidid(String androidid) {
		this.androidid = androidid;
	}

	public String getPhoneno() {
		return phoneno;
	}

	public void setPhoneno(String phoneno) {
		this.phoneno = phoneno;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EntityStatus getStatus() {
		return status;
	}

	public void setStatus(EntityStatus status) {
		this.status = status;
	}

	public String getNewpassword1() {
		return newpassword1;
	}

	public void setNewpassword1(String newpassword1) {
		this.newpassword1 = newpassword1;
	}

	public String getNewpassword2() {
		return newpassword2;
	}

	public void setNewpassword2(String newpassword2) {
		this.newpassword2 = newpassword2;
	}

	public int getPasswordreset() {
		return passwordreset;
	}

	public void setPasswordreset(int passwordreset) {
		this.passwordreset = passwordreset;
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

	public long getWebuserid() {
		return webuserid;
	}

	public void setWebuserid(long webuserid) {
		this.webuserid = webuserid;
	}

}
