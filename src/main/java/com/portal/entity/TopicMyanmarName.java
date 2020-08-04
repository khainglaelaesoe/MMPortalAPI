package com.portal.entity;

public enum TopicMyanmarName {
	
	Health ("ကျန်းမာရေး"),
	Education_Research ("ပညာရေးနှင့် သုတေသန"),
	Social ("လူမှုရေး"),
	Economy ("စီးပွားရေး"),
	Agriculture ("စိုက်ပျိုးရေး"),
	Labour_Employment ("အလုပ်သမားရေးရာ"),
	Livestock ("မွေးမြူရေးနှင့် ရေလုပ်ငန်း"),
	Law_Justice ("ဥပဒေနှင့် တရားရေး"),
	Security ("လုံခြုံရေး"),
	Hotel_Tourism ("ဟိုတယ်နှင့် ခရီးသွားလာရေး"),
	Citizen ("နိုင်ငံသား"),
	Natural_Resources_Environment ("သဘာဝသယံဇာတနှင့်ပတ်ဝန်းကျင်"),
	Industries ("စက်မှုလုပ်ငန်း"),
	Construction ("ဆောက်လုပ်ရေး"),
	Science ("သိပ္ပံနည်းပညာ"),
	Technology ("နည်းပညာ"),
	Transportation ("ပို့ဆောင်ရေး"),
	Communication ("ဆက်သွယ်ရေး"),
	Information_Media ("သတင်းအချက်အလက်နှင့် မီဒီယာများ"),
	Religion_Art_Culture ("ဘာသာ၊ အနုပညာနှင့် ယဉ်ကျေးမှု"),
	Finance_Tax ("ဘဏ္ဍာရေးနှင့်အခွန်"),
	SMEs ("အသေးစား၊ အလတ်စား လုပ်ငန်းများ"),
	Natural_Disaster ("သဘာဝဘေးအန္တရာယ်"),
	Power_Energy ("လျှပ်စစ်နှင့် စွမ်းအင်"),
	Sports ("အားကစား"),
	Statistics ("စာရင်းအင်း"),
	Insurances ("အာမခံ"),
	City_Development ("စည်ပင်သာယာရေး"),
	Visas_Passports ("ဗီဇာနှင့်ပတ်စပို့");
	
	private String value;

	private TopicMyanmarName(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
