package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.PollsChoiceDao;
import com.portal.entity.PollsChoice;

@Repository
public class PollsChoiceDaoImpl extends AbstractDaoImpl<PollsChoice, String> implements PollsChoiceDao {

	protected PollsChoiceDaoImpl() {
		super(PollsChoice.class);
	}

}
