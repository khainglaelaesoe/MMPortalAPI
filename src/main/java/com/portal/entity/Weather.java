package com.portal.entity;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonView;

public class Weather {
	@JsonView(Views.Summary.class)
	@Transient
	String myantitle;
	
	@JsonView(Views.Summary.class)
	@Transient
	String engtitle;
	
	@JsonView(Views.Summary.class)
	@Transient
	String myancontent;
	
	@JsonView(Views.Summary.class)
	@Transient
	String engcontent;

	public String getMyantitle() {
		return myantitle;
	}
	public void setMyantitle(String myantitle) {
		this.myantitle = myantitle;
	}
	public String getEngtitle() {
		return engtitle;
	}
	public void setEngtitle(String engtitle) {
		this.engtitle = engtitle;
	}
	public String getMyancontent() {
		return myancontent;
	}
	public void setMyancontent(String myancontent) {
		this.myancontent = myancontent;
	}
	public String getEngcontent() {
		return engcontent;
	}
	public void setEngcontent(String engcontent) {
		this.engcontent = engcontent;
	}
	
}
