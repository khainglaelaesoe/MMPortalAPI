package com.portal.entity;

public enum TopicEngName {
	
	Health ("Health"),
	Education_Research ("Education & Research"),
	Social ("Social"),
	Economy ("Economy"),
	Agriculture ("Agriculture"),
	Labour_Employment ("Labour & Employment"),
	Livestock ("Livestock"),
	Law_Justice ("Law & Justice"),
	Security ("Security"),
	Hotel_Tourism ("Hotel & Tourism"),
	Citizen ("Citizen"),
	Natural_Resources_Environment ("Natural Resources & Environment"),
	Industries ("Industries"),
	Construction ("Construction"),
	Science ("Science"),
	Technology ("Technology"),
	Transportation ("Transportation"),
	Communication ("Communication"),
	Information_Media ("Information & Media"),
	Religion_Art_Culture ("Religion, Art & Culture"),
	Finance_Tax ("Finance & Tax"),
	SMEs ("SMEs "),
	Natural_Disaster ("Natural Disaster"),
	Power_Energy ("Power & Energy"),
	Sports ("Sports"),
	Statistics ("Statistics"),
	Insurances ("Insurances"),
	City_Development ("City Development"),
	Visas_Passports ("Visas & Passports");	

	private String value;

	private TopicEngName(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
