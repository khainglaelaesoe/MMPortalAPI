package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "PollsChoice")
public class PollsChoice implements Serializable {

	@Id
	@Column(name = "choiceid", unique = true, nullable = false)
	private long choiceid;

	private String description;
	
	@Transient
	private long choicecount;
	
	public long getChoiceid() {
		return choiceid;
	}

	public void setChoiceid(long choiceid) {
		this.choiceid = choiceid;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getChoicecount() {
		return choicecount;
	}

	public void setChoicecount(long choicecount) {
		this.choicecount = choicecount;
	}

}
