package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.AssetCategoryDao;
import com.portal.entity.AssetCategory;

@Repository
public class AssetCategoryDaoImpl extends AbstractDaoImpl<AssetCategory, String> implements AssetCategoryDao {

	protected AssetCategoryDaoImpl() {
		super(AssetCategory.class);
	}

}
