package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "DDMStructure")
public class DDMStructure implements Serializable {

	@Id
	@Column(name = "structureid", unique = true, nullable = false)
	private long structureid;

	private String definition;
	
	@Transient
	private long choicecount;

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public long getStructureid() {
		return structureid;
	}

	public void setStructureid(long structureid) {
		this.structureid = structureid;
	}

	public long getChoicecount() {
		return choicecount;
	}

	public void setChoicecount(long choicecount) {
		this.choicecount = choicecount;
	}

	
}
