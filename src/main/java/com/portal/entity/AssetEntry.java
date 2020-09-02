package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "AssetEntry")
public class AssetEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "entryId", unique = true, nullable = false)
	private long entryid;

	@Column(name = "userid")
	private long userid;

	@JsonIgnore
	@Column(name = "username")
	private String username;

	@JsonIgnore
	@Column(name = "createdate")
	private String createdate;

	@JsonIgnore
	@Column(name = "modifieddate")
	private String modifieddate;

	@JsonIgnore
	@Column(name = "classUuid")
	private String classuuid;

	@JsonIgnore
	@Column(name = "classtypeid")
	private Long classtypeid;

	@JsonIgnore
	@Column(name = "publishdate")
	private String publishdate;

	@JsonView(Views.Thin.class)
	@Column(name = "title")
	private String title;

	@JsonIgnore
	@Column(name = "summary")
	private String summary;

	@JsonIgnore
	@Column(name = "layoutuuid")
	private String layoutuuid;

	@JsonIgnore
	@Column(name = "priority")
	private double priority;

	@JsonIgnore
	@Column(name = "viewcount")
	private int viewcount;

	@JsonIgnore
	@Column(name = "visible")
	private int visible;

	@JsonIgnore
	@Column(name = "classNameId")
	private Long classnameid;

	@JsonIgnore
	@Column(name = "classPK")
	private Long classpk;

	public long getEntryid() {
		return entryid;
	}

	public void setEntryid(long entryid) {
		this.entryid = entryid;
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

	public String getClassuuid() {
		return classuuid;
	}

	public void setClassuuid(String classuuid) {
		this.classuuid = classuuid;
	}

	public Long getClasstypeid() {
		return classtypeid;
	}

	public void setClasstypeid(Long classtypeid) {
		this.classtypeid = classtypeid;
	}

	public String getPublishdate() {
		return publishdate;
	}

	public void setPublishdate(String publishdate) {
		this.publishdate = publishdate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLayoutuuid() {
		return layoutuuid;
	}

	public void setLayoutuuid(String layoutuuid) {
		this.layoutuuid = layoutuuid;
	}

	public double getPriority() {
		return priority;
	}

	public void setPriority(double priority) {
		this.priority = priority;
	}

	public int getViewcount() {
		return viewcount;
	}

	public void setViewcount(int viewcount) {
		this.viewcount = viewcount;
	}

	public int getVisible() {
		return visible;
	}

	public void setVisible(int visible) {
		this.visible = visible;
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

}
