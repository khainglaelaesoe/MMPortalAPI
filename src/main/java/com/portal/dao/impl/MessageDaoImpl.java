package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.MessageDao;
import com.portal.entity.MBMessage;

@Repository
public class MessageDaoImpl extends AbstractDaoImpl<MBMessage, String> implements MessageDao {

	protected MessageDaoImpl() {
		super(MBMessage.class);
	}
}
