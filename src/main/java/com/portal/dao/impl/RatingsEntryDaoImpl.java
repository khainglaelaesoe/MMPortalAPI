package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.RatingsEntryDao;
import com.portal.entity.RatingsEntry;

@Repository
public class RatingsEntryDaoImpl extends AbstractDaoImpl<RatingsEntry, String> implements RatingsEntryDao {

	protected RatingsEntryDaoImpl() {
		super(RatingsEntry.class);
	}

}
