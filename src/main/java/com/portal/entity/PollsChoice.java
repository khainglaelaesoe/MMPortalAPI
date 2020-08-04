package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PollsChoice")
public class PollsChoice implements Serializable {

	@Id
	@Column(name = "choiceid", unique = true, nullable = false)
	private long choiceid;

	private String description;

}
