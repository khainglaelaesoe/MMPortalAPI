package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.AssetEntryDao;
import com.portal.entity.AssetEntry;

@Repository
public class AssetEntryDaoImpl extends AbstractDaoImpl<AssetEntry, String> implements AssetEntryDao {

	protected AssetEntryDaoImpl() {
		super(AssetEntry.class);
	}

}
