package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PollsVote")
public class PollsVote implements Serializable {

	@Id
	@Column(name = "voteid", unique = true, nullable = false)
	private long voteid;

	private String userName;

	private long questionid;
	
	private long choiceid;
}
