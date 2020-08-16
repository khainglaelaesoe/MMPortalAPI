package com.portal.service;

import java.util.List;

import com.portal.entity.AssetEntry;
import com.portal.entity.MBMessage;

public interface AssetEntryService {
	public List<Long> getAssetEntryClassPkByClassTypeId(long classTypeId);

	public AssetEntry getAssetEntryByClassTypeCategoryTitle(long cid, String engtitle, String myantitle);

	public AssetEntry getAssetEntryMNREC(String title);

	public AssetEntry getAssetEntryByClassTypeCategoryIdTitle(long cid, long ctid, String title);

	public AssetEntry getAssetEntryByClassType(long cid);

	public AssetEntry getAssetEntry(long id);

	public List<Long> getClassPkList(long classTypeId);

	public List<String> getAssetEntryListForServices(long categoryId);

	public List<Long> getAssetEntryListForLiveStockService(long categoryId);

	public List<String> getAssetEntryListForTender(long categoryId);

	public List<Long> getClassPKsForForm(long categoryId);

	public List<String> getAssetEntryListForJobAndVacancy(long categoryId);

	public List<Long> getAssetEntryListForDocument(long categoryId);

	public List<Long> getAssetEntryListForLiveStockJobAndVacancy(long categoryId);

	public List<String> getAssetEntryListForDocumentByAllTopic();

	public List<Long> getClassuuidListForPollAndSurvey(long categoryId);

	public List<Long> getClassNameByClassUuid(String classUuid);

	public List<String> getAssetEntryByTitle(String title);

	public List<Long> getClassPKListViewCount(long classTypeId);

	public List<Long> getAssetEntryListForServicesByViewCount(long categoryId);

	public List<Object> byClassTypeId(long classTypeId);

	public List<Long> getAssetEntryListForJobAndVacancyByViewCount(long categoryId);

	public List<String> getAssetEntryListByClassTypeId(long classTypeId);

	public List<Long> getAssetEntryListForTendersByViewCount(long categoryId);

	public List<Long> getClassName(String classUuid);

	public List<Long> getAssetEntryListForTendersByLatest(long categoryId);

	public List<Long> getAssetEntryListForJobAndVacancyByLatest(long categoryId);

	public List<Long> getAssetEntryListForServicesByLatest(long categoryId);

	public List<Long> getAssetEntryListByClassTypeIdAndOrderByPriority(long classTypeId);

	public List<Long> getAssetEntryListByName(long classTypeId, String searchTerm);

	public List<String> getClassUuidByDate(Long classTypeId, String dateStr);

	public long getClassPK();
	
	public List<Long> getClassPKListbyCatagoryId();

}
