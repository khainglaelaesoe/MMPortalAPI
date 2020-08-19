package com.portal.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

public class RequestVote {

	@JsonView(Views.Thin.class)
	List<PollsChoice> pollsChoiceList;

	@JsonView(Views.Thin.class)
	String userid;

	@JsonView(Views.Thin.class)
	String pollOrSurveyId;

	@JsonView(Views.Thin.class)
	String totalVoteCount;

	@JsonView(Views.Thin.class)
	String webuserid;

	@JsonView(Views.Thin.class)
	private String userstatus;

	private List<Long> messageid;

	@JsonView(Views.Thin.class)
	List<MBMessage> mbmessagelist;

	@JsonView(Views.Thin.class)
	List<Long> classpklist;

	@JsonView(Views.Thin.class)
	String totalNotiCount;

	List<JournalArticle> journalArticle;

	private List<MBMessage> commentList;

	public List<MBMessage> getCommentList() {
		if (commentList == null)
			this.commentList = new ArrayList<MBMessage>();
		return commentList;
	}

	public void setCommentList(List<MBMessage> commentList) {
		this.commentList = commentList;
	}

	public List<PollsChoice> getPollsChoiceList() {
		return pollsChoiceList;
	}

	public void setPollsChoiceList(List<PollsChoice> pollsChoiceList) {
		this.pollsChoiceList = pollsChoiceList;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPollOrSurveyId() {
		return pollOrSurveyId;
	}

	public void setPollOrSurveyId(String pollOrSurveyId) {
		this.pollOrSurveyId = pollOrSurveyId;
	}

	public String getTotalVoteCount() {
		return totalVoteCount;
	}

	public void setTotalVoteCount(String totalVoteCount) {
		this.totalVoteCount = totalVoteCount;
	}

	public String getWebuserid() {
		return webuserid;
	}

	public void setWebuserid(String webuserid) {
		this.webuserid = webuserid;
	}

	public String getUserstatus() {
		return userstatus;
	}

	public void setUserstatus(String userstatus) {
		this.userstatus = userstatus;
	}

	public List<Long> getMessageid() {
		return messageid;
	}

	public void setMessageid(List<Long> messageid) {
		this.messageid = messageid;
	}

	public String getTotalNotiCount() {
		return totalNotiCount;
	}

	public void setTotalNotiCount(String totalNotiCount) {
		this.totalNotiCount = totalNotiCount;
	}

	public List<MBMessage> getMbmessagelist() {
		return mbmessagelist;
	}

	public void setMbmessagelist(List<MBMessage> mbmessagelist) {
		this.mbmessagelist = mbmessagelist;
	}

	public List<Long> getClasspklist() {
		return classpklist;
	}

	public void setClasspklist(List<Long> classpklist) {
		this.classpklist = classpklist;
	}

	public List<JournalArticle> getJournalArticle() {
		return journalArticle;
	}

	public void setJournalArticle(List<JournalArticle> journalArticle) {
		this.journalArticle = journalArticle;
	}

}
