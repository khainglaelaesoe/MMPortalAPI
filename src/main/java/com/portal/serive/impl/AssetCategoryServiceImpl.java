package com.portal.serive.impl;

import java.util.List;
import java.util.Locale.Category;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
		String queryStr = "select assetCategory from AssetCategory assetCategory where vocabularyid=80291 and parentcategoryid=" + id + " order by leftcategoryid";
		return assetCategoryDao.byQuery(queryStr);
	}

	@Override
	public AssetCategory getAssetCategoryByParentCategoryIdandName(long id, String name) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where vocabularyid=80291 and parentcategoryid=" + id + " and name like '%" + name + "%'";
		logger.info("queryStr !!" + queryStr);
		List<AssetCategory> categories = assetCategoryDao.byQuery(queryStr);
		if (CollectionUtils.isEmpty(categories))
			return null;
		return categories.get(0);
	}

	@Override
	public List<AssetCategory> getAssetCategoryByParentCategoryIdMinistry(long id) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where vocabularyid=80291 and" + " name!='No Longer In Use Minitries' and parentcategoryid=" + id + " order by leftcategoryid";
		return assetCategoryDao.byQuery(queryStr);
	}

	@Override
	public AssetCategory getAssetCategoryByParentCategoryIdMinistryName(long id, String name) {
		String queryStr = "select assetCategory from AssetCategory assetCategory where vocabularyid=80291 and" + " name!='No Longer In Use Minitries' and parentcategoryid=" + id + " and name like '%" + name + "%'";
		return assetCategoryDao.byQuery(queryStr).get(0);
	}

	public List<Long> getIdListByParentCategoryIdMinistry(long id) {
		String queryStr = "select assetCategory.categoryid from AssetCategory assetCategory where vocabularyid=80291 and" + " name!='No Longer In Use Minitries' and parentcategoryid=" + id + " order by leftcategoryid";
		return assetCategoryDao.findLongByQueryString(queryStr);
	}

	public List<String> getHluttaws() {
		String queryStr = "Select name from AssetCategory where vocabularyId=80291 and parentCategoryId=8249564 order by leftCategoryId desc";
		return assetCategoryDao.findByQuery(queryStr);
	}

	public List<String> getGovs() {
		String queryStr = "Select name from AssetCategory where vocabularyId=80291 and parentCategoryId=87195 order by leftCategoryId";
		List<String> names = assetCategoryDao.findByQuery(queryStr);
		names.add("Yangon Region Government");
		return names;
	}

	public List<String> getGovOrgs() {
		String queryStr = "Select name from AssetCategory where vocabularyId=80291 and parentCategoryId=80624 order by leftCategoryId";
		return assetCategoryDao.findByQuery(queryStr);
	}

	public List<String> getGovOrgs2() {
		String queryStr = "Select name from AssetCategory where vocabularyId=80291 and parentCategoryId=87166 order by leftCategoryId";
		return assetCategoryDao.findByQuery(queryStr);
	}
}
