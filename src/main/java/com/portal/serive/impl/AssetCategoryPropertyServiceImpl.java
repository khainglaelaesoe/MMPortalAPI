package com.portal.serive.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.portal.dao.AssetCategoryPropertyDao;
import com.portal.entity.AssetCategoryProperty;
import com.portal.service.AssetCategoryPropertyService;

@Service("assetCategoryPropertyService")
public class AssetCategoryPropertyServiceImpl implements AssetCategoryPropertyService {

	@Autowired
	private AssetCategoryPropertyDao assetCategoryPropertyDao;

	private static Logger logger = Logger.getLogger(AssetCategoryPropertyServiceImpl.class);

	// KLLS
	// Select * from realportal.assetcategoryproperty where categoryId=?
	@Override
	public List<AssetCategoryProperty> getAssetCategoryPropertyByCategoryId(long catid) {
		String queryStr = "select assetCategoryProperty from AssetCategoryProperty assetCategoryProperty where categoryid="
				+ catid;
		return assetCategoryPropertyDao.byQuery(queryStr);
	}

}
