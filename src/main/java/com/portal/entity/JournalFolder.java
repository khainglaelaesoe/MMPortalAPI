package com.portal.entity;

import javax.persistence.Column;
import javax.persistence.Id;

public class JournalFolder {

	@Id
	@Column(name = "folderid", unique = true, nullable = false)
	private long folderid;

	@Column(name = "name")
	private String name;

	public long getFolderid() {
		return folderid;
	}

	public void setFolderid(long folderid) {
		this.folderid = folderid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
