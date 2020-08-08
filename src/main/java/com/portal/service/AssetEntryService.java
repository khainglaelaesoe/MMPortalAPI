package com.portal.service;

import java.util.List;

import com.portal.entity.AssetEntry;
import com.portal.entity.MBMessage;

public interface AssetEntryService {

	public AssetEntry getAssetEntryByClassTypeCategoryTitle(long cid, String engtitle, String myantitle);

	public AssetEntry getAssetEntryMNREC(String title);

	public AssetEntry getAssetEntryByClassTypeCategoryIdTitle(long cid, long ctid, String title);

	public AssetEntry getAssetEntryByClassType(long cid);

	public AssetEntry getAssetEntry(long id);

	public List<String> getAssetEntryListByClassTypeId(long classTypeId);

	public List<String> getAssetEntryListForServices(long categoryId);

	public List<String> getAssetEntryListForLiveStockService(long categoryId);

	public List<String> getAssetEntryListForTender(long categoryId);

	public List<String> getAssetEntryListForForm(long categoryId);

	public List<String> getAssetEntryListForJobAndVacancy(long categoryId);

	public List<String> getAssetEntryListForDocument(long categoryId);

	public List<String> getAssetEntryListForLiveStockJobAndVacancy(long categoryId);

	public List<String> getAssetEntryListForDocumentByAllTopic();

	public List<String> getClassuuidListForPollAndSurvey(long categoryId);

	public List<Long> getClassNameByClassUuid(String classUuid);

	public List<String> getAssetEntryByTitle(String title);

	public List<String> getAssetEntryListByClassTypeIdAndViewCount(long classTypeId);

	public List<String> getAssetEntryListForServicesByViewCount(long categoryId);

	public List<Object> byClassTypeId(long classTypeId);

	public List<String> getAssetEntryListForJobAndVacancyByViewCount(long categoryId);

	public List<String> getAssetEntryListForTendersByViewCount(long categoryId);

	public List<Long> getClassName(String classUuid);

	public List<Long> getClassPK(String classUuid);

	public List<String> getAssetEntryListForTendersByLatest(long categoryId);

	public List<String> getAssetEntryListForJobAndVacancyByLatest(long categoryId);

	public List<String> getAssetEntryListForServicesByLatest(long categoryId);

	public long getClassPK();

	public List<String> getAssetEntryListByClassTypeIdAndOrderByPriority(long classTypeId);

	public List<String> getAssetEntryListByName(long classTypeId, String searchTerm);
	
	public List<String> getClassUuidByDate(Long classTypeId, String dateStr);


}
