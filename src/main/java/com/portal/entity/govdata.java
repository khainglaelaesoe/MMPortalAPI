package com.portal.entity;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

public class govdata {
	@Transient
	@JsonView(Views.Thin.class)
	String deptNo; 
	
	@Transient
	@JsonView(Views.Thin.class)
	String dept;
	
	@Transient
	@JsonView(Views.Thin.class)
	String website;
	
	public String getDeptNo() {
		return deptNo;
	}
	public void setDeptNo(String deptNo) {
		this.deptNo = deptNo;
	}
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}

	
}
