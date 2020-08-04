package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "DDMStructure")
public class DDMStructure implements Serializable {

	@Id
	@Column(name = "structureid", unique = true, nullable = false)
	private long structureid;

	private String definition;

}
