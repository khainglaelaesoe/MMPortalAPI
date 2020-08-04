package com.portal.serive.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.RatingsEntryDao;
import com.portal.entity.RatingsEntry;
import com.portal.service.RatingsEntryService;

@Service("ratingsEntryService")
public class RatingsEntryServiceImpl implements RatingsEntryService {

	@Autowired
	private RatingsEntryDao ratingsEntryDao;

	public List<RatingsEntry> getScoresByClass(Long classNameId, Long classPk) {
		String query = "from RatingsEntry entry where classNameId=" + classNameId + " and classPk=" + classPk;
		return ratingsEntryDao.getAll(query);
	}
}
