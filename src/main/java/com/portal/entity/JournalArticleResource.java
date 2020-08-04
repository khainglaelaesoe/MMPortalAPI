package com.portal.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "JournalArticleResource")
public class JournalArticleResource implements Serializable {
	private static final long serialVersionUID = 1L;

	@Column(name = "uuid_")
	private String uuid_;

	@Id
	@Column(name = "resourceprimkey", unique = true, nullable = false)
	private long resourceprimkey;

	@Column(name = "articleid")
	private long articleid;

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

	public long getArticleid() {
		return articleid;
	}

	public void setArticleid(long articleid) {
		this.articleid = articleid;
	}

}
