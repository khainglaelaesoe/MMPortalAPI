package com.portal.entity;

public enum OrgEngName {

	all("Government Organizations"),	
	Government_Organizations("Government Organizations"),
	
	President_Office("President Office"),
	Union_Government_Office("Union Government Office"),
	Pyidaungsu_Hluttaw("Pyidaungsu Hluttaw"),
	Pyithu_Hluttaw("Pyithu Hluttaw"),
	Amyotha_Hluttaw("Amyotha Hluttaw"), 
	State_Regional_Hluttaws("State & Regional Hluttaws"),
	The_Supreme_Court_of_the_Union("The Supreme Court of the Union"), 
	Constitutional_Tribunal("Constitutional Tribunal of the Union of Myanmar"),
	Union_Election_Commission("Union Election Commission"),
	Ministry_of_Foreign_Affairs("Ministry of Foreign Affairs"),
	Ministry_of_President_Office("Ministry of President Office"),
	Ministry_of_the_Office_of_the_State_Counsellor("Ministry of the Office of the State Counsellor"),
	Ministry_of_Union_Government_Office("Ministry of Union Government Office"),
	Ministry_of_Home_Affairs("Ministry of Home Affairs"),
	Ministry_of_Defence("Ministry of Defense"),
	Ministry_of_Border_Affairs("Ministry of Border Affairs"),
	Ministry_of_Planning_Finance_and_Industry("Ministry of Planning, Finance and Industry"),
	Ministry_of_Investment_and_Foreign_Economic_Relations("Ministry of Investment and Foreign Economic Relations"),
	Ministry_of_International_Cooperation("Ministry of International Cooperation"),	 
	Ministry_of_Information("Ministry of Information"),
	Ministry_of_Religious_Affairs_and_Culture("Ministry of Religious Affairs & Culture"),
	Ministry_of_Agriculture_Livestocks_and_Irrigation("Ministry of Agriculture, Livestock and Irrigation"),
	Ministry_of_Transport_and_Communications("Ministry of Transport and Communications"),
	Ministry_of_Natural_Resources_and_Environmental_Conservation("Ministry of Natural Resources and Environmental Conservation"),
	Ministry_of_Electricity_and_Energy("Ministry of Electricity & Energy"),
	Ministry_of_Labour_Immigration_and_Population("Ministry of Labour, Immigration and Population"),
	Ministry_of_Commerce("Ministry of Commerce"),
	Ministry_of_Education("Ministry of Education"),
	Ministry_of_Health_and_Sports("Ministry of Health & Sports"),
	Ministry_of_Construction("Ministry of Construction"),
	Ministry_of_Social_Welfare_Relief_Resettlement("Ministry of Social Welfare, Relief & Resettlement"),
	Ministry_of_Hotel_and_Tourism("Ministry of Hotels & Tourism"),
	Ministry_of_Ethnic_Affairs("Ministry of Ethnic Affairs"),
	Union_Attonery_Generals_Office("Union Attorney General's Office"),
	Office_of_the_Auditor_General_of_the_Union("Office of the Auditor General of the Union"),
	Union_Civil_Service_Board("Union Civil Service Board"),
	Naypyitaw_Council("Naypyitaw Council"),
	Region_and_State_Government("Region & State Government"),
	Central_Bank("Central Bank"),
	Revenue_Appellate_Tribunal("Revenue Appellate Tribunal"),
	Anti_Corruption_Commission("Anti-corruption Commission"),
	Myanmar_Nation_Human_Rights_Commission("Myanmar National Human Rights Commission"),
	Napyitaw_City_Development_Committee("Naypyitaw Development Committee"),
	Yangon_City_Development_Committee("Yangon City Development Committee"),
	Mandalay_City_Development_Committee("Mandalay City Development Committee"),
	
	
	Kachin_State_Government("Kachin State Government"),
	Kayah_State_Government("Kayah State Government"), Kayin_State_Government("Kayin State Government"), Chin_State_Government("Chin State Government"),
	Sagaing_Region_Government("Sagaing Region Government"), Tanintaryi_Region_Government("Tanintaryi Region Government"), Bago_Region_Government("Bago Region Government"), Magway_Region_Government("Magway Region Government"), Mandalay_Region_Government("Mandalay Region Government"), Mon_State_Government("Mon State Government"), Rakhine_State_Government("Rakhine State Government"), Yangon_Region_Government("Yangon Region Government"), Shan_State_Government("Shan State Government"),
	Ayeyarwaddy_Region_Government("Ayeyarwaddy Region Government"),	
	Ministry_of_Planning_and_Finance("Ministry of Planning and Finance"),Topics(""),
	Myanmar_Computer_Federation(" Myanmar Computer Federation");

	private String value;

	private OrgEngName(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
