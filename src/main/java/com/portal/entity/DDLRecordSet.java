package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DDLRecordSet")
public class DDLRecordSet implements Serializable {
	
	@Id
	@Column(name = "recordsetid", unique = true, nullable = false)
	private long recordsetid;
	
	private long ddmStructureid;

}
