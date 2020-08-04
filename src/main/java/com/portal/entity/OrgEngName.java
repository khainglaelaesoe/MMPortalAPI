package com.portal.entity;

public enum OrgEngName {

	President_Office("President Office"), 
	Union_Government_Office("Union Government Office"),
	Pyidaungsu_Hluttaw("Pyidaungsu Hluttaw"),
	Pyithu_Hluttaw("Pyithu Hluttaw"),	
	Amyotha_Hluttaw("Amyotha Hluttaw"),
	The_Supreme_Court_of_the_Union("The Supreme Court of the Union"),
	Constitutional_Tribunal("Constitutional Tribunal"), 
	Union_Election_Commission("Union Election Commission"),
	Ministry_of_Foreign_Affairs("Ministry of Foreign Affairs"),
	Ministry_of_President_Office("Ministry of President Office"),
	Ministry_of_Home_Affairs("Ministry of Home Affairs"),
	Ministry_of_Defence("Ministry of Defence"),
	Ministry_of_Border_Affairs("Ministry of Boder Affairs"),	
	Anti_Corruption_Commission("Anti-Corruption Commission"),
	Ayeyarwaddy_Region_Government("Ayeyarwady Region Government"), Bago_Region_Government("Bago Region Government"),
	Central_Bank("Central Bank"), Chin_State_Government("Chin State Government"),
    Kachin_State_Government("Kachin State Government"),
	Kayah_State_Government("Kayah State Government"), Kayin_State_Government("Kayin State Government"),
	Magway_Region_Government("Magway Region Government"),
	Mandalay_City_Development_Committee("Mandalay City Development Committee"),
	Mandalay_Region_Government("Mandalay Region Government"),
	Ministry_of_Agriculture_Livestocks_and_Irrigation("Ministry of Agriculture, Livestock and Irrigation"),
	Ministry_of_Commerce("Ministry of Commerce"),
	Ministry_of_Construction("Ministry of Construction"),
	Ministry_of_Education("Ministry of Education"),
	Ministry_of_Electricity_and_Energy("Ministry of Electricity and Energy"),
	Ministry_of_Ethnic_Affairs("Ministry of Ethnic Affairs"),
	Ministry_of_Health_and_Sports("Ministry of Health and Sports"),
	Ministry_of_Hotel_and_Tourism("Ministry of Hotels and Tourism"), 
	Ministry_of_Information("Ministry of Information"),
	Ministry_of_International_Cooperation("Ministry of International Cooperation"),
	Ministry_of_Investment_and_Foreign_Economic_Relations("Ministry of Investment and Foreign Economic Relations"),
	Ministry_of_Labour_Immigration_and_Population("Ministry of Labour, Immigration and Population"),
	Ministry_of_Natural_Resources_and_Environmental_Conservation("Ministry of Natural Resources and Environmental Conservation"),
	Ministry_of_Planning_and_Finance("Ministry of Planning, Finance and Industry"),
	Ministry_of_Planning_Finance_and_Industry("Ministry of Planning, Finance and Industry"),
	Ministry_of_Religious_Affairs_and_Culture("Ministry of Religious Affairs and Culture"),
	Ministry_of_Social_Welfare_Relief_Resettlement("Ministry of Social Welfare, Relief & Resettlement"),
	Ministry_of_the_Office_of_the_State_Counsellor("Ministry of the Office of the State Counsellor"),
	Ministry_of_Transport_and_Communications("Ministry of Transport and Communications"),
	Ministry_of_Union_Government_Office("Ministry of Union Government Office"),
	Mon_State_Government("Mon State Government"), Naypyitaw_Council("Naypyitaw Council"),
	Napyitaw_City_Development_Committee("Naypyitaw Development Committee"),
	Office_of_the_Auditor_General_of_the_Union("Office of the Auditor General of the Union"),
	 
	Rakhine_State_Government("Rakhine State Government"), Sagaing_Region_Government("Sagaing Region Government"),
	Shan_State_Government("Shan State Government"), Tanintaryi_Region_Government("Tanintaryi Region Government"),
	
	Union_Attonery_Generals_Office("Union Attorney General?s Office"),
	Union_Civil_Service_Board("Union Civil Service Board"),
	
	Yangon_City_Development_Committee("Yangon City Development Committee"),
	Yangon_Region_Government("Yangon Region Government"), Home(""),
	Myanmar_Computer_Federation("Myanmar Computer Federation"),
	Myanmar_Nation_Human_Rights_Commission("Myanmar Nation Human Rights Commission"), Topics("");

	private String value;

	private OrgEngName(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
