package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "AssetEntries_AssetCategories")
public class AssetEntries_AssetCategories implements Serializable  {

	@Id
	@Column(name = "entryid", unique = true, nullable = false)
	private long entryid;

	@Column(name = "categoryid")
	private long categoryid;

	@Column(name = "companyid")
	private long companyid;

	public long getEntryid() {
		return entryid;
	}

	public void setEntryid(long entryid) {
		this.entryid = entryid;
	}

	public long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(long categoryid) {
		this.categoryid = categoryid;
	}

	public long getCompanyid() {
		return companyid;
	}

	public void setCompanyid(long companyid) {
		this.companyid = companyid;
	}

}
