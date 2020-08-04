package com.portal.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "Organization_")
public class Organization_ implements Serializable {

	private static final long serialVersionUID = 1L;

	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<TableData> tableMyanData;

	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<TableData> tableEngData;

	@Transient
	@JsonView(Views.Thin.class)
	private String myanEmail;

	@Transient
	@JsonView(Views.Thin.class)
	private String engContentTitle;

	@Transient
	@JsonView(Views.Thin.class)
	private String myanmarContentTitle;

	@Transient
	@JsonView(Views.Thin.class)
	private String engContent;

	@Transient
	@JsonView(Views.Thin.class)
	private String mmContent;

	@Transient
	@JsonView(Views.Thin.class)
	private String content;

	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<Organization_> seeMore;

	@Column(name = "uuid_")
	private String uuid_;

	@Id
	@JsonIgnore
	@Column(name = "organizationid", unique = true, nullable = false)
	private long organizationid;

	@JsonIgnore
	@Column(name = "companyid")
	private long companyid;

	@JsonIgnore
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
	@Column(name = "parentorganizationid")
	private long parentorganizationid;

	@JsonIgnore
	@Column(name = "treepath")
	private String treepath;

	@JsonIgnore
	@Column(name = "type_")
	private String type_;

	@JsonIgnore
	@Column(name = "recursable")
	private int recursable;

	@JsonIgnore
	@Column(name = "regionid")
	private long regionid;

	@JsonIgnore
	@Column(name = "countryid")
	private long countryid;

	@JsonIgnore
	@Column(name = "statusid")
	private long statusid;

	@JsonIgnore
	@Column(name = "comments")
	private String comments;

	@JsonIgnore
	@Column(name = "logoid")
	private long logoid;

	@Transient
	@JsonView(Views.Thin.class)
	private String myanmarName;

	@Transient
	@JsonView(Views.Thin.class)
	private String engName;

	@Transient
	@JsonView(Views.Summary.class)
	private String myanmarPhoneNo;

	@Transient
	@JsonView(Views.Summary.class)
	private List<String> myanmarPhoneNoList;

	@Transient
	@JsonView(Views.Summary.class)
	private List<String> engPhoneNoList;

	@Transient
	@JsonView(Views.Summary.class)
	private String engPhoneNo;

	@Transient
	@JsonView(Views.Summary.class)
	private String email;

	@Transient
	@JsonView(Views.Summary.class)
	private String myanmarAddress;

	@Transient
	@JsonView(Views.Summary.class)
	private String engAddress;

	@Transient
	@JsonView(Views.Thin.class)
	private String key;

	@Transient
	@JsonView(Views.Thin.class)
	private String image;

	public String getUuid_() {
		return uuid_;
	}

	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}

	public long getOrganizationid() {
		return organizationid;
	}

	public void setOrganizationid(long organizationid) {
		this.organizationid = organizationid;
	}

	public long getCompanyid() {
		return companyid;
	}

	public void setCompanyid(long companyid) {
		this.companyid = companyid;
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

	public long getParentorganizationid() {
		return parentorganizationid;
	}

	public void setParentorganizationid(long parentorganizationid) {
		this.parentorganizationid = parentorganizationid;
	}

	public String getTreepath() {
		return treepath;
	}

	public void setTreepath(String treepath) {
		this.treepath = treepath;
	}

	public String getType_() {
		return type_;
	}

	public void setType_(String type_) {
		this.type_ = type_;
	}

	public int getRecursable() {
		return recursable;
	}

	public void setRecursable(int recursable) {
		this.recursable = recursable;
	}

	public long getRegionid() {
		return regionid;
	}

	public void setRegionid(long regionid) {
		this.regionid = regionid;
	}

	public long getCountryid() {
		return countryid;
	}

	public void setCountryid(long countryid) {
		this.countryid = countryid;
	}

	public long getStatusid() {
		return statusid;
	}

	public void setStatusid(long statusid) {
		this.statusid = statusid;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public long getLogoid() {
		return logoid;
	}

	public void setLogoid(long logoid) {
		this.logoid = logoid;
	}

	public String getMyanmarName() {
		return myanmarName;
	}

	public void setMyanmarName(String myanmarName) {
		this.myanmarName = myanmarName;
	}

	public String getEngName() {
		return engName;
	}

	public void setEngName(String engName) {
		this.engName = engName;
	}

	public String getMyanmarPhoneNo() {
		return myanmarPhoneNo;
	}

	public void setMyanmarPhoneNo(String myanmarPhoneNo) {
		this.myanmarPhoneNo = myanmarPhoneNo;
	}

	public String getEngPhoneNo() {
		return engPhoneNo;
	}

	public void setEngPhoneNo(String engPhoneNo) {
		this.engPhoneNo = engPhoneNo;
	}

	public String getMyanmarAddress() {
		return myanmarAddress;
	}

	public void setMyanmarAddress(String myanmarAddress) {
		this.myanmarAddress = myanmarAddress;
	}

	public String getEngAddress() {
		return engAddress;
	}

	public void setEngAddress(String engAddress) {
		this.engAddress = engAddress;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getEngContent() {
		return engContent;
	}

	public void setEngContent(String engContent) {
		this.engContent = engContent;
	}

	public String getMmContent() {
		return mmContent;
	}

	public void setMmContent(String mmContent) {
		this.mmContent = mmContent;
	}

	public String getEngContentTitle() {
		return engContentTitle;
	}

	public void setEngContentTitle(String engContentTitle) {
		this.engContentTitle = engContentTitle;
	}

	public String getMyanmarContentTitle() {
		return myanmarContentTitle;
	}

	public void setMyanmarContentTitle(String myanmarContentTitle) {
		this.myanmarContentTitle = myanmarContentTitle;
	}

	public String getMyanEmail() {
		return myanEmail;
	}

	public void setMyanEmail(String myanEmail) {
		this.myanEmail = myanEmail;
	}

	public ArrayList<TableData> getTableMyanData() {
		return tableMyanData;
	}

	public void setTableMyanData(ArrayList<TableData> tableMyanData) {
		this.tableMyanData = tableMyanData;
	}

	public ArrayList<TableData> getTableEngData() {
		return tableEngData;
	}

	public void setTableEngData(ArrayList<TableData> tableEngData) {
		this.tableEngData = tableEngData;
	}

	public ArrayList<Organization_> getSeeMore() {
		return seeMore;
	}

	public void setSeeMore(ArrayList<Organization_> seeMore) {
		this.seeMore = seeMore;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getMyanmarPhoneNoList() {
		if (myanmarPhoneNoList == null)
			myanmarPhoneNoList = new ArrayList<String>();
		return myanmarPhoneNoList;
	}

	public void setMyanmarPhoneNoList(List<String> myanmarPhoneNoList) {
		this.myanmarPhoneNoList = myanmarPhoneNoList;
	}

	public List<String> getEngPhoneNoList() {
		if (engPhoneNoList == null)
			engPhoneNoList = new ArrayList<String>();
		return engPhoneNoList;
	}

	public void setEngPhoneNoList(List<String> engPhoneNoList) {
		this.engPhoneNoList = engPhoneNoList;
	}

}
