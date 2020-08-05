package com.portal.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "JournalArticle")
public class JournalArticle extends AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id_", unique = true, nullable = false)
	@JsonView(Views.Thin.class)
	private long id_;

	@Column(name = "uuid_")
	private String uuid_;

	@JsonIgnore
	@Column(name = "resourceprimkey")
	private long resourceprimkey;

	@JsonIgnore
	@Column(name = "groupid")
	private long groupid;

	@JsonIgnore
	@Column(name = "userid")
	private long userid;

	@JsonIgnore
	@Column(name = "username")
	private String username;

	@JsonIgnore
	@Column(name = "createdate")
	private String createdate;

	@JsonIgnore
	@Column(name = "modifieddate")
	private String modifieddate;

	@JsonIgnore
	@Column(name = "folderid")
	private long folderid;

	@JsonView(Views.Thin.class)
	@Column(name = "classpk")
	private String classpk;

	@JsonIgnore
	@Column(name = "treepath")
	private String treepath;

	@JsonIgnore
	@Column(name = "version")
	private double version;

	@JsonView(Views.Thin.class)
	@Column(name = "title")
	private String title;

	@JsonView(Views.Thin.class)
	@Transient
	private String mynamrTitle;

	@JsonView(Views.Thin.class)
	@Transient
	private String engTitle;

	@JsonView(Views.Thin.class)
	@Transient
	private String classNameString;
	
	@JsonView(Views.Thin.class)
	@Transient
	private String pKString;

	@JsonIgnore
	@Column(name = "urltitle")
	private String urltitle;

	@JsonIgnore
	@Column(name = "description")
	private String description;

	// @JsonView(Views.Summary.class)
	@JsonView(Views.Thin.class)
	@Column(name = "content")
	private String content;

	@Transient
	@JsonView(Views.Thin.class)
	private String myanmarContent;

	@Transient
	@JsonView(Views.Thin.class)
	private String engContent;

	@JsonIgnore
	@Column(name = "ddmstructurekey")
	private String ddmstructurekey;

	@Column(name = "ddmtemplatekey")
	private String ddmtemplatekey;

	@Column(name = "layoutuuid")
	private String layoutuuid;

	@JsonView(Views.Thin.class)
	@Column(name = "displaydate")
	private String displaydate;

	@JsonView(Views.Thin.class)
	@Transient
	private String myanmarLocation;

	@JsonView(Views.Thin.class)
	@Transient
	private String engLocation;

	@JsonIgnore
	@Column(name = "expirationdate")
	private String expirationdate;

	@JsonIgnore
	@Column(name = "indexable")
	private byte indexable;

	@JsonIgnore
	@Column(name = "smallimage")
	private byte smallimage;

	@JsonIgnore
	@Column(name = "smallimageid")
	private long smallimageid;

	@JsonIgnore
	@Column(name = "smallimageurl")
	private String smallimageurl;

	@JsonIgnore
	@Column(name = "status")
	private int status;

	@JsonIgnore
	@Column(name = "statusbyuserid")
	private long statusbyuserid;

	@JsonIgnore
	@Column(name = "statusbyusername")
	private String statusbyusername;

	// @JsonIgnore
	@JsonView(Views.Thin.class)
	@Column(name = "articleid")
	private long articleid;

	@JsonView(Views.Thin.class)
	@Transient
	private String imageUrl;

	@JsonView(Views.Summary.class)
	@Transient
	private String DepartmentTitle;

	@JsonView(Views.Thin.class)
	@Transient
	private String myanmarDepartmentTitle;

	@JsonView(Views.Thin.class)
	@Transient
	private String engDepartmentTitle;

	@JsonView(Views.Summary.class)
	@Transient
	private String myanmarPublisher;

	@JsonView(Views.Summary.class)
	@Transient
	private String engPblisher;

	@JsonView(Views.Summary.class)
	@Transient
	private String publicationDate;

	@JsonView(Views.Summary.class)
	@Transient
	private String page;

	@JsonView(Views.Thin.class)
	@Transient
	private String myanamrDownloadLink;

	@JsonView(Views.Thin.class)
	@Transient
	private String engDownloadLink;

	@JsonView(Views.Thin.class)
	@Transient
	private String myanamrImageUrl;

	@JsonView(Views.Thin.class)
	@Transient
	private String engImageUrl;

	@JsonView(Views.Thin.class)
	@Transient
	private String videoLink;

	@JsonView(Views.Thin.class)
	@Transient
	private String myanmarOnlineForm;

	@JsonView(Views.Thin.class)
	@Transient
	private String engOnlineForm;

	@Transient
	@JsonView(Views.Thin.class)
	private String iosEngContent;

	@Transient
	@JsonView(Views.Thin.class)
	private String iosMyaContent;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> myanmarNameList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> myanmarLinkList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> engNameList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> engLinkList;

	@Transient
	@JsonView(Views.Thin.class)
	private HashMap<Integer, String> iOSmNameList;

	@Transient
	@JsonView(Views.Thin.class)
	private HashMap<Integer, String> iOSmLinkList;

	@Transient
	@JsonView(Views.Thin.class)
	private HashMap<Integer, String> iOSeNameList;

	@Transient
	@JsonView(Views.Thin.class)
	private HashMap<Integer, String> iOSeLinkList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> mNameList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> mLinkList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> eNameList;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> eLinkList;

	@Transient
	@JsonView(Views.Thin.class)
	private String shareLink;

	@Transient
	@JsonView(Views.Thin.class)
	private double rating;

	@Transient
	@JsonView(Views.Thin.class)
	private double userRating;

	@Transient
	@JsonView(Views.Thin.class)
	private String ratingAction;

	@Transient
	@JsonView(Views.Thin.class)
	private List<MBMessage> messageList;

	@Transient
	@JsonView(Views.Thin.class)
	private long pollOrSurveyCount;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> myanmarQuestions;

	@Transient
	@JsonView(Views.Thin.class)
	private List<String> EngQuestions;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String questionid;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String userstatus;
	
	@Transient
	@JsonView(Views.Thin.class)
	private List<Map<String, String>> myanmarQuestionsMap;

	@Transient
	@JsonView(Views.Thin.class)
	private List<Map<String, String>> EngQuestionsMap;

	public String getRatingAction() {
		return ratingAction;
	}

	public void setRatingAction(String ratingAction) {
		this.ratingAction = ratingAction;
	}

	public HashMap<Integer, String> getiOSmNameList() {
		return iOSmNameList;
	}

	public void setiOSmNameList(HashMap<Integer, String> iOSmNameList) {
		this.iOSmNameList = iOSmNameList;
	}

	public HashMap<Integer, String> getiOSmLinkList() {
		return iOSmLinkList;
	}

	public void setiOSmLinkList(HashMap<Integer, String> iOSmLinkList) {
		this.iOSmLinkList = iOSmLinkList;
	}

	public HashMap<Integer, String> getiOSeNameList() {
		return iOSeNameList;
	}

	public void setiOSeNameList(HashMap<Integer, String> iOSeNameList) {
		this.iOSeNameList = iOSeNameList;
	}

	public HashMap<Integer, String> getiOSeLinkList() {
		return iOSeLinkList;
	}

	public void setiOSeLinkList(HashMap<Integer, String> iOSeLinkList) {
		this.iOSeLinkList = iOSeLinkList;
	}

	public List<String> getmNameList() {
		return mNameList;
	}

	public void setmNameList(List<String> mNameList) {
		this.mNameList = mNameList;
	}

	public List<String> getmLinkList() {
		return mLinkList;
	}

	public void setmLinkList(List<String> mLinkList) {
		this.mLinkList = mLinkList;
	}

	public List<String> geteNameList() {
		return eNameList;
	}

	public void seteNameList(List<String> eNameList) {
		this.eNameList = eNameList;
	}

	public List<String> geteLinkList() {
		return eLinkList;
	}

	public void seteLinkList(List<String> eLinkList) {
		this.eLinkList = eLinkList;
	}

	public long getId_() {
		return id_;
	}

	public void setId_(long id_) {
		this.id_ = id_;
	}

	public String getUuid_() {
		return uuid_;
	}

	public void setUuid_(String uuid_) {
		this.uuid_ = uuid_;
	}

	public long getResourceprimkey() {
		return resourceprimkey;
	}

	public void setResourceprimkey(long resourceprimkey) {
		this.resourceprimkey = resourceprimkey;
	}

	public long getGroupid() {
		return groupid;
	}

	public void setGroupid(long groupid) {
		this.groupid = groupid;
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

	public long getFolderid() {
		return folderid;
	}

	public void setFolderid(long folderid) {
		this.folderid = folderid;
	}

	public String getTreepath() {
		return treepath;
	}

	public void setTreepath(String treepath) {
		this.treepath = treepath;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrltitle() {
		return urltitle;
	}

	public void setUrltitle(String urltitle) {
		this.urltitle = urltitle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDdmstructurekey() {
		return ddmstructurekey;
	}

	public void setDdmstructurekey(String ddmstructurekey) {
		this.ddmstructurekey = ddmstructurekey;
	}

	public String getDdmtemplatekey() {
		return ddmtemplatekey;
	}

	public void setDdmtemplatekey(String ddmteplatekey) {
		this.ddmtemplatekey = ddmteplatekey;
	}

	public String getLayoutuuid() {
		return layoutuuid;
	}

	public void setLayoutuuid(String layoutuuid) {
		this.layoutuuid = layoutuuid;
	}

	public String getDepartmentTitle() {
		return DepartmentTitle;
	}

	public void setDepartmentTitle(String departmentTitle) {
		DepartmentTitle = departmentTitle;
	}

	public byte getIndexable() {
		return indexable;
	}

	public void setIndexable(byte indexable) {
		this.indexable = indexable;
	}

	public byte getSmallimage() {
		return smallimage;
	}

	public void setSmallimage(byte smallimage) {
		this.smallimage = smallimage;
	}

	public long getSmallimageid() {
		return smallimageid;
	}

	public void setSmallimageid(long smallimageid) {
		this.smallimageid = smallimageid;
	}

	public String getSmallimageurl() {
		return smallimageurl;
	}

	public void setSmallimageurl(String smallimageurl) {
		this.smallimageurl = smallimageurl;
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

	public long getArticleid() {
		return articleid;
	}

	public void setArticleid(long articleid) {
		this.articleid = articleid;
	}

	public String getCreatedate() {
		return createdate;
	}

	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}

	public String getModifieddate() {
		return modifieddate;
	}

	public void setModifieddate(String modifieddate) {
		this.modifieddate = modifieddate;
	}

	public String getDisplaydate() {
		return displaydate;
	}

	public void setDisplaydate(String displaydate) {
		this.displaydate = displaydate;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getMynamrTitle() {
		return mynamrTitle;
	}

	public void setMynamrTitle(String mynamrTitle) {
		this.mynamrTitle = mynamrTitle;
	}

	public String getEngTitle() {
		return engTitle;
	}

	public void setEngTitle(String engTitle) {
		this.engTitle = engTitle;
	}

	public String getMyanmarPublisher() {
		return myanmarPublisher;
	}

	public void setMyanmarPublisher(String myanmarPublisher) {
		this.myanmarPublisher = myanmarPublisher;
	}

	public String getEngPblisher() {
		return engPblisher;
	}

	public void setEngPblisher(String engPblisher) {
		this.engPblisher = engPblisher;
	}

	public String getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(String publishionDate) {
		this.publicationDate = publishionDate;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getMyanmarDownloadLink() {
		return myanamrDownloadLink;
	}

	public void setMyanmarDownloadLink(String downloadLink) {
		this.myanamrDownloadLink = downloadLink;
	}

	public String getMyanamrImageUrl() {
		return myanamrImageUrl;
	}

	public void setMyanamrImageUrl(String myanamrImageUrl) {
		this.myanamrImageUrl = myanamrImageUrl;
	}

	public String getEngImageUrl() {
		return engImageUrl;
	}

	public void setEngImageUrl(String engImageUrl) {
		this.engImageUrl = engImageUrl;
	}

	public String getEngDownloadLink() {
		return engDownloadLink;
	}

	public void setEngDownloadLink(String engDownloadLink) {
		this.engDownloadLink = engDownloadLink;
	}

	public String getMyanamrDownloadLink() {
		return myanamrDownloadLink;
	}

	public void setMyanamrDownloadLink(String myanamrDownloadLink) {
		this.myanamrDownloadLink = myanamrDownloadLink;
	}

	public String getVideoLink() {
		return videoLink;
	}

	public void setVideoLink(String videoLink) {
		this.videoLink = videoLink;
	}

	public String getMyanmarDepartmentTitle() {
		return myanmarDepartmentTitle;
	}

	public void setMyanmarDepartmentTitle(String myanmarDepartmentTitle) {
		this.myanmarDepartmentTitle = myanmarDepartmentTitle;
	}

	public String getEngDepartmentTitle() {
		return engDepartmentTitle;
	}

	public void setEngDepartmentTitle(String engDepartmentTitle) {
		this.engDepartmentTitle = engDepartmentTitle;
	}

	public String getMyanmarContent() {
		return myanmarContent;
	}

	public void setMyanmarContent(String myanmarContent) {
		this.myanmarContent = myanmarContent;
	}

	public String getEngContent() {
		return engContent;
	}

	public void setEngContent(String engContent) {
		this.engContent = engContent;
	}

	public String getMyanmarOnlineForm() {
		return myanmarOnlineForm;
	}

	public void setMyanmarOnlineForm(String myanmarOnlineForm) {
		this.myanmarOnlineForm = myanmarOnlineForm;
	}

	public String getEngOnlineForm() {
		return engOnlineForm;
	}

	public void setEngOnlineForm(String engOnlineForm) {
		this.engOnlineForm = engOnlineForm;
	}

	public String getMyanmarLocation() {
		return myanmarLocation;
	}

	public void setMyanmarLocation(String myanmarLocation) {
		this.myanmarLocation = myanmarLocation;
	}

	public String getEngLocation() {
		return engLocation;
	}

	public void setEngLocation(String engLocation) {
		this.engLocation = engLocation;
	}

	public List<String> getMyanmarNameList() {
		return myanmarNameList;
	}

	public void setMyanmarNameList(List<String> myanmarNameList) {
		this.myanmarNameList = myanmarNameList;
	}

	public List<String> getMyanmarLinkList() {
		return myanmarLinkList;
	}

	public void setMyanmarLinkList(List<String> myanmarLinkList) {
		this.myanmarLinkList = myanmarLinkList;
	}

	public List<String> getEngNameList() {
		return engNameList;
	}

	public void setEngNameList(List<String> engNameList) {
		this.engNameList = engNameList;
	}

	public List<String> getEngLinkList() {
		return engLinkList;
	}

	public void setEngLinkList(List<String> engLinkList) {
		this.engLinkList = engLinkList;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public String getShareLink() {
		return shareLink;
	}

	public void setShareLink(String shareLink) {
		this.shareLink = shareLink;
	}

	public List<MBMessage> getMessageList() {
		if (messageList == null)
			messageList = new ArrayList<MBMessage>();
		return messageList;
	}

	public void setMessageList(List<MBMessage> messageList) {
		this.messageList = messageList;
	}

	public long getPollOrSurveyCount() {
		return pollOrSurveyCount;
	}

	public void setPollOrSurveyCount(long pollOrSurveyCount) {
		this.pollOrSurveyCount = pollOrSurveyCount;
	}

	public List<String> getMyanmarQuestions() {
		return myanmarQuestions;
	}

	public void setMyanmarQuestions(List<String> myanmarQuestions) {
		this.myanmarQuestions = myanmarQuestions;
	}

	public List<String> getEngQuestions() {
		return EngQuestions;
	}

	public void setEngQuestions(List<String> engQuestions) {
		EngQuestions = engQuestions;
	}

	public String getIosEngContent() {
		return iosEngContent;
	}

	public void setIosEngContent(String iosEngContent) {
		this.iosEngContent = iosEngContent;
	}

	public String getIosMyaContent() {
		return iosMyaContent;
	}

	public void setIosMyaContent(String iosMyaContent) {
		this.iosMyaContent = iosMyaContent;
	}

	@Override
	public String toString() {
		return "JournalArticle [title=" + title + "]";
	}

	public String getClasspk() {
		return classpk;
	}

	public void setClasspk(String classpk) {
		this.classpk = classpk;
	}
	
	public String getClassNameString() {
		return classNameString;
	}

	public void setClassNameString(String classNameString) {
		this.classNameString = classNameString;
	}

	public String getpKString() {
		return pKString;
	}

	public void setpKString(String pKString) {
		this.pKString = pKString;
	}

	public double getUserRating() {
		return userRating;
	}

	public void setUserRating(double userRating) {
		this.userRating = userRating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

	public String getQuestionid() {
		return questionid;
	}

	public void setQuestionid(String questionid) {
		this.questionid = questionid;
	}

	public String getUserstatus() {
		return userstatus;
	}

	public void setUserstatus(String userstatus) {
		this.userstatus = userstatus;
	}

	public List<Map<String, String>> getMyanmarQuestionsMap() {
		return myanmarQuestionsMap;
	}

	public void setMyanmarQuestionsMap(List<Map<String, String>> myanmarQuestionsMap) {
		this.myanmarQuestionsMap = myanmarQuestionsMap;
	}

	public List<Map<String, String>> getEngQuestionsMap() {
		return EngQuestionsMap;
	}

	public void setEngQuestionsMap(List<Map<String, String>> engQuestionsMap) {
		EngQuestionsMap = engQuestionsMap;
	}
	
}
