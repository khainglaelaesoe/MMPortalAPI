package com.portal.service;

import java.util.List;

import com.portal.entity.AssetCategoryProperty;



public interface AssetCategoryPropertyService {
	public List<AssetCategoryProperty> getAssetCategoryPropertyByCategoryId(long catid);
}
