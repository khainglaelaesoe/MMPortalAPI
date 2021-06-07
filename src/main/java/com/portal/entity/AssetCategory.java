package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "AssetCategory")
public class AssetCategory implements Serializable {

	@Id
	@Column(name = "categoryid", unique = true, nullable = false)
	private long categoryid;

	@JsonIgnore
	@Column(name = "userid")
	private long userid;

	@JsonIgnore
	@JsonView(Views.Summary.class)
	@Column(name = "name")
	private String name;

	@JsonIgnore
	@JsonView(Views.Summary.class)
	@Column(name = "title")
	private String title;
	
	@JsonIgnore
	@JsonView(Views.Summary.class)
	@Column(name = "leftcategoryid")
	private String leftcategoryid;

	@JsonView(Views.Thin.class)
	@Transient
	private String audioLink;

	@JsonIgnore
	@JsonView(Views.Summary.class)
	@Column(name = "vocabularyid")
	private long vocabularyid;

	@JsonView(Views.Summary.class)
	@Transient
	private String imageurl;

	@JsonView(Views.Summary.class)
	@Transient
	private String detailurl;

	@JsonView(Views.Thin.class)
	@Transient
	private String engtitle;

	@JsonView(Views.Thin.class)
	@Transient
	private String myantitle;

	@JsonView(Views.Thin.class)
	@Transient
	private String engcontent;

	@JsonView(Views.Thin.class)
	@Transient
	private String myancontent;

	@Transient
	@JsonView(Views.Thin.class)
	private String iosEngContent;

	@Transient
	@JsonView(Views.Thin.class)
	private String iosMyaContent;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String language;

	private long parentcategoryid;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String base64image;

	@Transient
	@JsonView(Views.Thin.class)
	private String articleImage;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String engImageUrl;
	
	@Transient
	@JsonView(Views.Thin.class)
	private String myanImageUrl;
	
	
	
	
	public String getLeftcategoryid() {
		return leftcategoryid;
	}

	public void setLeftcategoryid(String leftcategoryid) {
		this.leftcategoryid = leftcategoryid;
	}

	public String getEngImageUrl() {
		return engImageUrl;
	}

	public void setEngImageUrl(String engImageUrl) {
		this.engImageUrl = engImageUrl;
	}

	public String getMyanImageUrl() {
		return myanImageUrl;
	}

	public void setMyanImageUrl(String myanImageUrl) {
		this.myanImageUrl = myanImageUrl;
	}

	public String getBase64image() {
		return base64image;
	}

	public void setBase64image(String base64image) {
		this.base64image = base64image;
	}

	public String getArticleImage() {
		return articleImage;
	}

	public void setArticleImage(String articleImage) {
		this.articleImage = articleImage;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public long getParentcategoryid() {
		return parentcategoryid;
	}

	public void setParentcategoryid(long parentcategoryid) {
		this.parentcategoryid = parentcategoryid;
	}

	public long getCategoryid() {
		return categoryid;
	}

	public void setCategoryid(long categoryid) {
		this.categoryid = categoryid;
	}

	public String getAudioLink() {
		return audioLink;
	}

	public void setAudioLink(String audioLink) {
		this.audioLink = audioLink;
	}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getVocabularyid() {
		return vocabularyid;
	}

	public void setVocabularyid(long vocabularyid) {
		this.vocabularyid = vocabularyid;
	}

	public String getImageurl() {
		return imageurl;
	}

	public void setImageurl(String imageurl) {
		this.imageurl = imageurl;
	}

	public String getDetailurl() {
		return detailurl;
	}

	public void setDetailurl(String detailurl) {
		this.detailurl = detailurl;
	}

	public String getEngtitle() {
		return engtitle;
	}

	public void setEngtitle(String engtitle) {
		this.engtitle = engtitle;
	}

	public String getMyantitle() {
		return myantitle;
	}

	public void setMyantitle(String myantitle) {
		this.myantitle = myantitle;
	}

	public String getEngcontent() {
		if(engcontent == null)
			engcontent = "";
		return engcontent;
	}

	public void setEngcontent(String engcontent) {
		this.engcontent = engcontent;
	}

	public String getMyancontent() {
		if(myancontent == null)
			myancontent = "";
		return myancontent;
	}

	public void setMyancontent(String myancontent) {
		this.myancontent = myancontent;
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

}