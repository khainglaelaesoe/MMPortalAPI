package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.AssetCategoryPropertyDao;
import com.portal.entity.AssetCategoryProperty;

@Repository
public class AssetCategoryPropertyDaoImpl extends AbstractDaoImpl<AssetCategoryProperty, String> implements AssetCategoryPropertyDao {

	protected AssetCategoryPropertyDaoImpl() {
		super(AssetCategoryProperty.class);
	}
}