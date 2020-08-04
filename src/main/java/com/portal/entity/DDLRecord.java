package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DDLRecord")
public class DDLRecord implements Serializable {

	@Id
	@Column(name = "recordid", unique = true, nullable = false)
	private long recordid;

	private String userName;

	private long recordsetid;

	public long getRecordsetid() {
		return recordsetid;
	}

	public void setRecordsetid(long recordsetid) {
		this.recordsetid = recordsetid;
	}

}
