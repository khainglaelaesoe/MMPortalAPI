package com.portal.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "MBMessage")
public class MBMessage extends AbstractEntity {

	@Transient
	@JsonView(Views.Thin.class)
	private String notiStatus;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String profilepicture;
	
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

	@Column(name = "parentmessageid")
	private long parentmessageid;

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
	private String myaPostTitle;

	@Transient
	@JsonView(Views.Thin.class)
	private String engPostTitle;

	@Transient
	@JsonView(Views.Thin.class)
	private List<Reply> replyList;

	@Transient
	@JsonView(Views.Thin.class)
	private String editPermission;
	
	@Transient
	@JsonView(Views.Thin.class)
	private long notiid;

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

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

	public String getMyaPostTitle() {
		return myaPostTitle;
	}

	public void setMyaPostTitle(String myaPostTitle) {
		this.myaPostTitle = myaPostTitle;
	}

	public String getEngPostTitle() {
		return engPostTitle;
	}

	public void setEngPostTitle(String engPostTitle) {
		this.engPostTitle = engPostTitle;
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

	public List<Reply> getReplyList() {
		if (replyList == null)
			replyList = new ArrayList<Reply>();
		return replyList;
	}

	public void setReplyList(List<Reply> replyList) {
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

	public long getParentmessageid() {
		return parentmessageid;
	}

	public void setParentmessageid(long parentmessageid) {
		this.parentmessageid = parentmessageid;
	}

	public String getEditPermission() {
		return editPermission;
	}

	public void setEditPermission(String editPermission) {
		this.editPermission = editPermission;
	}

	public long getDislikecount() {
		return dislikecount;
	}

	public void setDislikecount(long dislikecount) {
		this.dislikecount = dislikecount;
	}

	public String getChecklike() {
		return checklike;
	}

	public void setChecklike(String checklike) {
		this.checklike = checklike;
	}

	public long getNotiid() {
		return notiid;
	}

	public void setNotiid(long notiid) {
		this.notiid = notiid;
	}
	
}
