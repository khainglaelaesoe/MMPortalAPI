package com.portal.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonView;

public class TableData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<String> Department;
	
	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<String> website;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String MiniStryNo;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String ministry;

	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<String> DeptNo;
	

	@Transient
	@JsonView(Views.Thin.class)
	private govdata[] DeptNoArr;
	
	@Transient
	@JsonView(Views.Thin.class)
	private govdata[] DepartmentArr;
	
	@Transient
	@JsonView(Views.Thin.class)
	private govdata[] websiteArr;
	
	@Transient
	@JsonView(Views.Thin.class)
	private ArrayList<govdata> dataArrList;
	
	public String getMiniStryNo() {
		return MiniStryNo;
	}

	public void setMiniStryNo(String miniStryNo) {
		MiniStryNo = miniStryNo;
	}

	public String getMinistry() {
		return ministry;
	}

	public void setMinistry(String ministry) {
		this.ministry = ministry;
	}


	public ArrayList<String> getDepartment() {
		return Department;
	}

	public void setDepartment(ArrayList<String> department) {
		Department = department;
	}

	public ArrayList<String> getWebsite() {
		return website;
	}

	public void setWebsite(ArrayList<String> website) {
		this.website = website;
	}

	public ArrayList<String> getDeptNo() {
		return DeptNo;
	}

	public void setDeptNo(ArrayList<String> deptNo) {
		DeptNo = deptNo;
	}

	public govdata[] getDeptNoArr() {
		return DeptNoArr;
	}

	public void setDeptNoArr(govdata[] deptNoArr) {
		DeptNoArr = deptNoArr;
	}

	public govdata[] getDepartmentArr() {
		return DepartmentArr;
	}

	public void setDepartmentArr(govdata[] departmentArr) {
		DepartmentArr = departmentArr;
	}

	public govdata[] getWebsiteArr() {
		return websiteArr;
	}

	public void setWebsiteArr(govdata[] websiteArr) {
		this.websiteArr = websiteArr;
	}

	public ArrayList<govdata> getDataArrList() {
		return dataArrList;
	}

	public void setDataArrList(ArrayList<govdata> dataArrList) {
		this.dataArrList = dataArrList;
	}


}
