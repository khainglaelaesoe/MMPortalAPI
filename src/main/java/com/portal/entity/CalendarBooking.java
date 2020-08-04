package com.portal.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "CalendarBooking")
public class CalendarBooking extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonView(Views.Thin.class)
	@Transient
	private String mmTitle;
	
	@JsonView(Views.Thin.class)
	@Transient
	private String engTitle;
	
	@JsonView(Views.Thin.class)
	@Transient
	private String startmonthyear;

	@JsonView(Views.Thin.class)
	@Transient
	private String startdaymonth;
	
	@JsonView(Views.Thin.class)
	@Transient
	private String endmonthyear;

	@JsonView(Views.Thin.class)
	@Transient
	private String enddaymonth;
	
//	@JsonIgnore
//	@Column(name = "uuid_")
//	private String uuid_;
	
	@Id
	@Column(name = "calendarbookingid", unique = true, nullable = false)
	private long calendarbookingid;
	
	@JsonIgnore
	@Column(name = "groupid")
	private long groupid;
	
	@JsonIgnore
	@Column(name = "companyid")
	private long companyid;
	
	@JsonIgnore
	@Column(name = "userid")
	private long userid;
	
	@JsonIgnore
	@Column(name = "username")
	private String username;
	
	@JsonIgnore
	@Column(name = "createdate")
	private Date createdate;
	
	@JsonIgnore
	@Column(name = "modifieddate")
	private Date modifieddate;
	
	@JsonIgnore
	@Column(name = "resourceblockid")
	private long resourceblockid;
	
	@JsonIgnore
	@Column(name = "calendarid")
	private long calendarid;
	
	@JsonIgnore
	@Column(name = "calendarresourceid")
	private long calendarresourceid;
	
	@JsonIgnore
	@Column(name = "parentcalendarbookingid")
	private long parentcalendarbookingid;
	
	@JsonIgnore
	@Column(name = "veventuid")
	private String veventuid;
	
	@JsonIgnore
	@Column(name = "title")
	private String title;
	
	@JsonIgnore
	@Column(name = "description")
	private String description;
	
	@JsonIgnore
	@Column(name = "location")
	private String location;
	
	@JsonIgnore
	@Column(name = "starttime")
	private long starttime;
	
	@JsonIgnore
	@Column(name = "endtime")
	private long endtime;
	
	@JsonIgnore
	@Column(name = "allday")
	private byte allday;
	
	@JsonIgnore
	@Column(name = "recurrence")
	private String recurrence;
	
	@JsonIgnore
	@Column(name = "firstreminder")
	private long firstreminder;
	
	@JsonIgnore
	@Column(name = "firstremindertype")
	private String firstremindertype;
	
	@JsonIgnore
	@Column(name = "secondreminder")
	private long secondreminder;
	
	@JsonIgnore
	@Column(name = "secondremindertype")
	private String secondremindertype;
	
	@JsonIgnore
	@Column(name = "lastpublishdate")
	private Date lastpublishdate;
	
	@JsonIgnore
	@Column(name = "status")
	private int status;
	
	@JsonIgnore
	@Column(name = "statusbyuserid")
	private long statusbyuserid;
	
	@JsonIgnore
	@Column(name = "statusbyusername")
	private String statusbyusername;
	
	@JsonIgnore
	@Column(name = "statusdate")
	private Date statusdate;

	public String getMmTitle() {
		return mmTitle;
	}

	public void setMmTitle(String mmTitle) {
		this.mmTitle = mmTitle;
	}

	public String getEngTitle() {
		return engTitle;
	}

	public void setEngTitle(String engTitle) {
		this.engTitle = engTitle;
	}


	public long getGroupid() {
		return groupid;
	}

	public void setGroupid(long groupid) {
		this.groupid = groupid;
	}

	public long getCompanyid() {
		return companyid;
	}

	public void setCompanyid(long companyid) {
		this.companyid = companyid;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getCreatedate() {
		return createdate;
	}

	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}

	public Date getModifieddate() {
		return modifieddate;
	}

	public void setModifieddate(Date modifieddate) {
		this.modifieddate = modifieddate;
	}


	public long getCalendarid() {
		return calendarid;
	}

	public void setCalendarid(long calendarid) {
		this.calendarid = calendarid;
	}


	public long getParentcalendarbookingid() {
		return parentcalendarbookingid;
	}

	public void setParentcalendarbookingid(long parentcalendarbookingid) {
		this.parentcalendarbookingid = parentcalendarbookingid;
	}

	public String getVeventuid() {
		return veventuid;
	}

	public void setVeventuid(String veventuid) {
		this.veventuid = veventuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public long getStarttime() {
		return starttime;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public long getEndtime() {
		return endtime;
	}

	public void setEndtime(long endtime) {
		this.endtime = endtime;
	}

	public byte getAllday() {
		return allday;
	}

	public void setAllday(byte allday) {
		this.allday = allday;
	}

	public String getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(String recurrence) {
		this.recurrence = recurrence;
	}

	public long getFirstreminder() {
		return firstreminder;
	}

	public void setFirstreminder(long firstreminder) {
		this.firstreminder = firstreminder;
	}

	public long getSecondreminder() {
		return secondreminder;
	}

	public void setSecondreminder(long secondreminder) {
		this.secondreminder = secondreminder;
	}

	public String getSecondremindertype() {
		return secondremindertype;
	}

	public void setSecondremindertype(String secondremindertype) {
		this.secondremindertype = secondremindertype;
	}

	public Date getLastpublishdate() {
		return lastpublishdate;
	}

	public void setLastpublishdate(Date lastpublishdate) {
		this.lastpublishdate = lastpublishdate;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getStatusbyuserid() {
		return statusbyuserid;
	}

	public void setStatusbyuserid(long statusbyuserid) {
		this.statusbyuserid = statusbyuserid;
	}

	public String getStatusbyusername() {
		return statusbyusername;
	}

	public void setStatusbyusername(String statusbyusername) {
		this.statusbyusername = statusbyusername;
	}

	public Date getStatusdate() {
		return statusdate;
	}

	public void setStatusdate(Date statusdate) {
		this.statusdate = statusdate;
	}

	public long getResourceblockid() {
		return resourceblockid;
	}

	public void setResourceblockid(long resourceblockid) {
		this.resourceblockid = resourceblockid;
	}

	public long getCalendarresourceid() {
		return calendarresourceid;
	}

	public void setCalendarresourceid(long calendarresourceid) {
		this.calendarresourceid = calendarresourceid;
	}

	public String getFirstremindertype() {
		return firstremindertype;
	}

	public void setFirstremindertype(String firstremindertype) {
		this.firstremindertype = firstremindertype;
	}

	public String getStartmonthyear() {
		return startmonthyear;
	}

	public void setStartmonthyear(String startmonthyear) {
		this.startmonthyear = startmonthyear;
	}

	public String getStartdaymonth() {
		return startdaymonth;
	}

	public void setStartdaymonth(String startdaymonth) {
		this.startdaymonth = startdaymonth;
	}

	public String getEndmonthyear() {
		return endmonthyear;
	}

	public void setEndmonthyear(String endmonthyear) {
		this.endmonthyear = endmonthyear;
	}

	public String getEnddaymonth() {
		return enddaymonth;
	}

	public void setEnddaymonth(String enddaymonth) {
		this.enddaymonth = enddaymonth;
	}

}
