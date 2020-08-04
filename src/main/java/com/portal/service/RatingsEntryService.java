package com.portal.service;

import java.util.List;

import com.portal.entity.RatingsEntry;

public interface RatingsEntryService {

	public List<RatingsEntry> getScoresByClass(Long classNameId, Long classPk);
	
}
