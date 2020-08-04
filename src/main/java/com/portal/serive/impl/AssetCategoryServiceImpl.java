package com.portal.serive.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.AssetCategoryDao;
import com.portal.entity.AssetCategory;
import com.portal.service.AssetCategoryService;

@Service("assetCategoryService")
public class AssetCategoryServiceImpl implements AssetCategoryService {

	@Autowired
	private AssetCategoryDao assetCategoryDao;

	private static Logger logger = Logger.getLogger(AssetCategoryServiceImpl.class);

	@Override
	public AssetCategory getAssetCategoryById(long id) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where categoryid=" + id;
		return assetCategoryDao.byQuery(queryStr).get(0);
	}

	// KLLS
	// SELECT * FROM realportal.assetcategory where name!='Economy' and
	// vocabularyId=80289;
	@Override
	public List<AssetCategory> getAssetCategoryNameExceptionByVocalbularyId(long id) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where name!='Economy' and vocabularyid=" + id;
		return assetCategoryDao.byQuery(queryStr);
	}

	@Override
	public List<AssetCategory> getAssetCategoryByParentCategoryId(long id) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where vocabularyid=80291 and parentcategoryid=" + id;
		return assetCategoryDao.byQuery(queryStr);
	}

	@Override
	public List<AssetCategory> getAssetCategoryByParentCategoryIdMinistry(long id) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where vocabularyid=80291 and" + " name!='No Longer In Use Minitries' and parentcategoryid=" + id;
		return assetCategoryDao.byQuery(queryStr);
	}

}
