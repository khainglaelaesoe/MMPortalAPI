package com.portal.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "RatingsEntry")
public class RatingsEntry implements Serializable {

	@Id
	@Column(name = "entryid", unique = true, nullable = false)
	private long entryid;

	@JsonView(Views.Thin.class)
	private long userid;

	@JsonView(Views.Thin.class)
	private String username;

	@JsonView(Views.Thin.class)
	@Column(name = "createdate")
	private Date createdate;

	@JsonView(Views.Thin.class)
	@Column(name = "modifieddate")
	private Date modifieddate;

	@JsonView(Views.Thin.class)
	private Long classnameid;

	@JsonIgnore
	@Column(name = "classPK")
	private Long classpk;

	@JsonIgnore
	@Column(name = "score")
	private double score;

	public long getEntryid() {
		return entryid;
	}

	public void setEntryid(long entryid) {
		this.entryid = entryid;
	}

	public Long getClassnameid() {
		return classnameid;
	}

	public void setClassnameid(Long classnameid) {
		this.classnameid = classnameid;
	}

	public Long getClasspk() {
		return classpk;
	}

	public void setClasspk(Long classpk) {
		this.classpk = classpk;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

}
