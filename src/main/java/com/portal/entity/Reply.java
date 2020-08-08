package com.portal.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

public class Reply {

	@Transient
	@JsonView(Views.Thin.class)
	private String checklike;
	
	@Transient
	@JsonView(Views.Thin.class)
	private long dislikecount;
	
	@Transient
	@JsonView(Views.Thin.class)
	private long likecount;

	@Id
	@JsonView(Views.Thin.class)
	@Column(name = "messageid", unique = true, nullable = false)
	private long messageid;

	@JsonView(Views.Thin.class)
	@Column(name = "userid", unique = true, nullable = false)
	private long userid;

	@JsonView(Views.Thin.class)
	private String statusbyusername;

	@JsonView(Views.Thin.class)
	private String subject;

	@JsonView(Views.Thin.class)
	private String body;

	@JsonView(Views.Thin.class)
	private String username;

	@JsonView(Views.Thin.class)
	private String createdate;

	@JsonView(Views.Thin.class)
	private Long classpk;

	@Transient
	@JsonView(Views.Thin.class)
	private List<MBMessage> replyList;

	@Transient
	@JsonView(Views.Thin.class)
	private String editPermission;
	
	@JsonView(Views.Thin.class)
	@Column(name = "parentmessageid")
	private long parentmessageid;

	public long getMessageid() {
		return messageid;
	}

	public void setMessageid(long messageid) {
		this.messageid = messageid;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getStatusbyusername() {
		return statusbyusername;
	}

	public void setStatusbyusername(String statusbyusername) {
		this.statusbyusername = statusbyusername;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<MBMessage> getReplyList() {
		if (replyList == null)
			replyList = new ArrayList<MBMessage>();
		return replyList;
	}

	public void setReplyList(List<MBMessage> replyList) {
		this.replyList = replyList;
	}

	public long getLikecount() {
		return likecount;
	}

	public void setLikecount(long likecount) {
		this.likecount = likecount;
	}

	public String getCreatedate() {
		return createdate;
	}

	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}

	public Long getClasspk() {
		return classpk;
	}

	public void setClasspk(Long classpk) {
		this.classpk = classpk;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getEditPermission() {
		return editPermission;
	}

	public void setEditPermission(String editPermission) {
		this.editPermission = editPermission;
	}

	public String getChecklike() {
		return checklike;
	}

	public void setChecklike(String checklike) {
		this.checklike = checklike;
	}

	public long getDislikecount() {
		return dislikecount;
	}

	public void setDislikecount(long dislikecount) {
		this.dislikecount = dislikecount;
	}

	public long getParentmessageid() {
		return parentmessageid;
	}

	public void setParentmessageid(long parentmessageid) {
		this.parentmessageid = parentmessageid;
	}
	
	
	
}
