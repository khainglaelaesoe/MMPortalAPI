package com.portal.entity;

public enum TopicImage {
	
	Health ("health.jpg"),
	Education_Research ("health.jpeg"),
	Social ("health.jpeg"),
	Economy ("health.jpeg"),
	Agriculture ("health.jpeg"),
	Labour_Employment ("health.jpeg"),
	Livestock ("health.jpeg"),
	Law_Justice ("health.jpeg"),
	Security ("health.jpeg"),
	Hotel_Tourism ("health.jpeg"),
	Citizen ("health.jpeg"),
	Natural_Resources_Environment ("health.jpeg"),
	Industries ("health.jpeg"),
	Construction ("health.jpeg"),
	Science ("health.jpeg"),
	Technology ("health.jpeg"),
	Transportation ("health.jpeg"),
	Communication ("health.jpeg"),
	Information_Media ("health.jpeg"),
	Religion_Art_Culture ("health.jpeg"),
	Finance_Tax ("health.jpeg"),
	SMEs ("health.jpeg"),
	Natural_Disaster ("health.jpeg"),
	Power_Energy ("health.jpeg"),
	Sports ("health.jpeg"),
	Statistics ("health.jpeg"),
	Insurances ("health.jpeg"),
	City_Development ("health.jpeg"),
	Visas_Passports ("health.jpeg");	

	private String value;

	private TopicImage(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
