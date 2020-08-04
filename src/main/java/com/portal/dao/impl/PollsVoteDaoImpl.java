package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.PollsVoteDao;
import com.portal.entity.PollsVote;

@Repository
public class PollsVoteDaoImpl extends AbstractDaoImpl<PollsVote, String> implements PollsVoteDao {

	protected PollsVoteDaoImpl() {
		super(PollsVote.class);
	}

}
