package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "AssetCategoryProperty")
public class AssetCategoryProperty implements Serializable {

	@Id
	@Column(name = "categorypropertyid", unique = true, nullable = false)
	private long categorypropertyid;

	@Column(name = "categoryid")
	private long categoryid;
	
	@Column(name = "key_")
	private String key_;

	@JsonView(Views.Summary.class)
	@Column(name = "value")
	private String value;

	public long getCategorypropertyid() {
		return categorypropertyid;
	}

	public void setCategorypropertyid(long categorypropertyid) {
		this.categorypropertyid = categorypropertyid;
	}

	public long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(long categoryid) {
		this.categoryid = categoryid;
	}

	public String getKey_() {
		return key_;
	}

	public void setKey_(String key_) {
		this.key_ = key_;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	

}
