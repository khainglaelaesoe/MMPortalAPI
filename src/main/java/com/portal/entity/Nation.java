package com.portal.entity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum Nation {

	PYIDAUNGSU("Pyidaungsu Hluttaw","ပြည်ထောင်စုလွှတ်တော်",8248752),
	PYITHU("Pyithu Hluttaw", "ပြည်သူ့လွှတ်တော်",8248752),
	AMYOTHA("Amyotha Hluttaw","အမျိုးသားလွှတ်တော်", 8248752),
	SRHLUTTAW("State & Regional Hluttaws", "တိုင်းဒေသကြီး/ပြည်နယ်လွှတ်တော်", 8248752),
	KACHIN_HLUTTAW("Kachin State Hluttaw","ကချင်ပြည်နယ်လွှတ်တော်", 8249564),
	KAYAH_HLUTTAW("Kayah State Hluttaw","ကယားပြည်နယ်လွှတ်တော်", 8249564),
	KAYIN_HLUTTAW("Kayin State Hluttaw","ကရင်ပြည်နယ်လွှတ်တော်", 8249564),
	CHIN_HLUTTAW("Chin State Hluttaw","ချင်းပြည်နယ်လွှတ်တော်", 8249564),
	SAGAING_HLUTTAW("Sagaing Region Hluttaw","စစ်ကိုင်းတိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	TANINTARYI_HlUTTAW("Tanintaryi Region Hluttaw","တနင်္သာရီတိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	BAGO_HLUTTAW("Bago Region Hluttaw","ပဲခူးတိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	MAGWAY_HLUTTAW("Magway Region Hluttaw","မကွေးတိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	MANDALAY_HLUTTAW("Mandalay Region Hluttaw","မန္တလေးတိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	MON_HLUTTAW("Mon State Hluttaw","မွန်ပြည်နယ်လွှတ်တော်", 8249564),
	RAKHINE_HLUTTAW("Rakhine State Hluttaw","ရခိုင်ပြည်နယ်လွှတ်တော်", 8249564),
	YANGON_HLUTTAW("Yangon Region Hluttaw","ရန်ကုန်တိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	SHAN_HLUTTAW("Shan State Hluttaw","ရှမ်းပြည်နယ်လွှတ်တော်", 8249564),
	AYEWADDY_HLTUTTAW("Ayeyarwady Region Hluttaw","ဧရာဝတီတိုင်းဒေသကြီးလွှတ်တော်", 8249564),
	PRESIDENT_OFFICE("President Office","နိုင်ငံတော်သမ္မတရုံး", 80624),
	UNION_GOV("Union Government","ပြည်ထောင်စုအစိုးရအဖွဲ့", 80624),
	MINISTRIES("Ministries","ဝန်ကြီးဌာနများ", 80624),
	UNION_ATTRONEY("Union Attorney General's Office","ပြည်ထောင်စုရှေ့နေချုပ်ရုံး", 80624),
	AUDIOTOR_GENERAL("Office of the Auditor General of the Union","ပြည်ထောင်စုစာရင်းစစ်ချုပ်ရုံး", 80624),
	CIVIL_SERVICE("Union Civil Service Board","ပြည်ထောင်စုရာထူးဝန်အဖွဲ့", 80624),
	NAYPYITAW_COUNSIL("Naypyitaw Council","နေပြည်တော်ကောင်စီ", 80624),
	CENTRAL_BANK("Central Bank","မြန်မာနိုင်ငံတော်ဗဟိုဘဏ်", 80624),
	Revenue_Appellate_Tribunal("Revenue Appellate Tribunal","အခွန်အယူခံခုံအဖွဲ့", 80624),

	
	
	MOFA("Ministry of Foreign Affairs","နိုင်ငံခြားရေးဝန်ကြီးဌာန", 87166),
	MOPO("Ministry of President Office","သမ္မတရုံးဝန်ကြီးဌာန", 87166),
	MOSC("Ministry of the Office of the State Counsellor","နိုင်ငံတော်အတိုင်ပင်ခံရုံးဝန်ကြီးဌာန", 87166),
	MOUGO("Ministry of Union Government Office","ပြည်ထောင်စုအစိုးရအဖွဲ့ရုံးဝန်ကြီးဌာန", 87166),
	MOHA("Ministry of Home Affairs","ပြည်ထဲရေးဝန်ကြီးဌာန", 87166),
	MOD("Ministry of Defense","ကာကွယ်ရေးဝန်ကြီးဌာန", 87166),
	MOBA("Ministry of Border Affairs","နယ်စပ်ရေးရာဝန်ကြီးဌာန", 87166),
	MOPFI("Ministry of Planning, Finance and Industry","စီမံကိန်း၊ ဘဏ္ဍာရေးနှင့် စက်မှုဝန်ကြီးဌာန", 87166),
	MOIFER("Ministry of Investment and Foreign Economic Relations","ရင်းနှီးမြှုပ်နှံမှုနှင့် နိုင်ငံခြားစီးပွားဆက်သွယ်ရေးဝန်ကြီးဌာန", 87166),
	MOIC("Ministry of International Cooperation","အပြည်ပြည်ဆိုင်ရာပူးပေါင်းဆောင်ရွက်ရေးဝန်ကြီးဌာန", 87166),
	MOI("Ministry of Information","ပြန်ကြားရေးဝန်ကြီးဌာန", 87166),
	MORAC("Ministry of Religious Affairs & Culture","သာသနာရေးနှင့်ယဉ်ကျေးမှုဝန်ကြီးဌာန", 87166),
	MOALI("Ministry of Agriculture, Livestock and Irrigation","စိုက်ပျိုးရေး၊ မွေးမြူရေးနှင့်ဆည်မြောင်း ဝန်ကြီးဌာန", 87166),
	MOTC("Ministry of Transport and Communications","ပို့ဆောင်ရေးနှင့်ဆက်သွယ်ရေးဝန်ကြီးဌာန", 87166),
	MONREC("Ministry of Natural Resources & Environmental Conservation","သယံဇာတနှင့်သဘာဝပတ်ဝန်းကျင် ထိန်းသိမ်းရေး ဝန်ကြီးဌာန", 87166),
	MOEE("Ministry of Electricity & Energy","လျှပ်စစ်နှင့်စွမ်းအင်ဝန်ကြီးဌာန", 87166),
	MOLIP("Ministry of Labour, Immigration & Population","အလုပ်သမား၊ လူဝင်မှုကြီးကြပ်ရေးနှင့် ပြည်သူ့အင်အား ဝန်ကြီးဌာန", 87166),
	MOC("Ministry of Commerce","စီးပွားရေးနှင့် ကူးသန်းရောင်းဝယ်ရေး ဝန်ကြီးဌာန", 87166),
	MOE("Ministry of Education","ပညာရေးဝန်ကြီးဌာန", 87166),
	MOHS("Ministry of Health & Sports","ကျန်းမာရေးနှင့်အားကစားဝန်ကြီးဌာန", 87166),
	MOCons("Ministry of Construction","ဆောက်လုပ်ရေးဝန်ကြီးဌာန", 87166),
	MOSWRS("Minstry of Social Welfare, Relief & Resettlement","လူမှုဝန်ထမ်း၊ ကယ်ဆယ်ရေးနှင့် ပြန်လည်နေရာချထားရေးဝန်ကြီးဌာန", 87166),
	MOHT("Ministry of Hotel & Tourism","ဟိုတယ်နှင့်ခရီးသွားလာရေးဝန်ကြီးဌာန", 87166),
	MOEA("Ministry of Ethnic Affairs","တိုင်းရင်းသားလူမျိုးများရေးရာဝန်ကြီးဌာန", 87166),
	SRGOV("Region & State Government","တိုင်းဒေသကြီးနှင့်ပြည်နယ် အစိုးရအဖွဲ့", 8251623 ),
	UNION_GOV_OFFICE("Union Government Office","ပြည်ထောင်စုအစိုးရအဖွဲ့ရုံး", 8251623),
	KACHIN_GOV("Kachin State Government","ကချင်ပြည်နယ်အစိုးရအဖွဲ့", 87195),
	KAYAH_GOV("Kayah State Government","ကယားပြည်နယ်အစိုးရအဖွဲ့", 87195),
	KAYIN_GOV("Kayin State Government","ကရင်ပြည်နယ်အစိုးရအဖွဲ့", 87195),
	CHIN_GOV("Chin State Government","ချင်းပြည်နယ်အစိုးရအဖွဲ့", 87195),
	SAGAING_GOV("Sagaing Region Government","စစ်ကိုင်းတိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),
	TANINTARYI_GOV("Tanintaryi Region Government","တနင်္သာရီတိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),
	BAGO_GOV("Bago Region Government","ပဲခူးတိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),
	MAGWAY_GOV("Magway Region Government","မကွေးတိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),
	MANDALAY_GOV("Mandalay Region Government","မန္တလေးတိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),
	MON_GOV("Mon State Government","မွန်ပြည်နယ်အစိုးရအဖွဲ့", 87195),
	RAKHINE_GOV("Rakhine State Government","ရခိုင်ပြည်နယ်အစိုးရအဖွဲ့", 87195),	
	YANGON_GOV("Yangon Region Government","ရန်ကုန်တိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),	
	SHAN_GOV("Shan State Government","ရှမ်းပြည်နယ်အစိုးရအဖွဲ့", 87195),
	AYEYAWADDY_GOV("Ayeyarwaddy Region Government","ဧရာဝတီတိုင်းဒေသကြီးအစိုးရအဖွဲ့", 87195),
	SUPREME_COURT("The Supreme Court of the Union","ပြည်ထောင်စုတရားလွှတ်တော်ချုပ်", 80625),
	CONSTITUTIONAL_TRIBUNAL("Constitutional Tribunal of the Union of Myanmar","နိုင်ငံတော်ဖွဲ့စည်းပုံအခြေခံဥပဒေဆိုင်ရာခုံရုံး", 80625),
	UEC("Union Election Commission","ပြည်ထောင်စုရွေးကောက်ပွဲကော်မရှင်", 80626),
	ANTI_CORRUPTION("Anti-corruption Commission","အဂတိလိုက်စားမှုတိုက်ဖျက်ရေးကော်မရှင်", 80626),
	HUMAN_RIGHT("Myanmar National Human Rights Commission","မြန်မာနိုင်ငံအမျိုးသားလူ့အခွင့်အရေးကော်မရှင်", 80626),
	NDC("Naypyitaw Development Committee","နေပြည်တော်စည်ပင်သာယာရေးကော်မတီ", 80626),
	YCDC("Yangon City Development Committee","ရန်ကုန်မြို့တော်စည်ပင်သာယာရေးကော်မတီ", 80626),
	MCDC("Mandalay City Development Committee","မန္တလေးမြို့တော်စည်ပင်သာယာရေးကော်မတီ", 80626),
	MCF("Myanmar Computer Federation","မြန်မာနိုင်ငံကွန်ပျူတာအသင်းချုပ်", 9590639);

	 public static Stream<Nation> stream() {
	        return Stream.of(Nation.values()); 
	    }
	 
	public String getEngName() {
		return EngName;
	}

	public String getMyanName() {
		return MyanName;
	}
	

	public int getCategoryId() {
		return CategoryId;
	}


	private String EngName;
	private String MyanName;
	private int CategoryId;

	private Nation(String eng, String myan, int id) {
		this.EngName = eng;
		this.MyanName=myan;
		this.CategoryId=id;
	}

	
	
}
