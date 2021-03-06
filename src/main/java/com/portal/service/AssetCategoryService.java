package com.portal.service;

import java.util.List;

import com.portal.entity.AssetCategory;

public interface AssetCategoryService {
	public AssetCategory getAssetCategoryById(long id);

	List<AssetCategory> getAssetCategoryNameExceptionByVocalbularyId(long id);

	public List<AssetCategory> getAssetCategoryByParentCategoryId(long id);

	public AssetCategory getAssetCategoryByParentCategoryIdandName(long id, String name);

	public List<AssetCategory> getAssetCategoryByParentCategoryIdMinistry(long id);

	public List<Long> getIdListByParentCategoryIdMinistry(long id);

	public AssetCategory getAssetCategoryByParentCategoryIdMinistryName(long id, String name);

	public List<String> getHluttaws();

	public List<String> getGovs();

	public List<String> getGovOrgs();
	
	public List<String> getGovOrgs2();


}
