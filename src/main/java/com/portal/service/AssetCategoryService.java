package com.portal.service;

import java.util.List;

import com.portal.entity.AssetCategory;

public interface AssetCategoryService {
	public AssetCategory getAssetCategoryById(long id);

	// KLLS
	List<AssetCategory> getAssetCategoryNameExceptionByVocalbularyId(long id);

	public List<AssetCategory> getAssetCategoryByParentCategoryId(long id);

	public List<AssetCategory> getAssetCategoryByParentCategoryIdMinistry(long id);
	
	public List<Long> getIdListByParentCategoryIdMinistry(long id);

}
