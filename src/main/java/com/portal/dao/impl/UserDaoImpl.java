package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.UserDao;
import com.portal.entity.User_;

@Repository
public class UserDaoImpl extends AbstractDaoImpl<User_, String> implements UserDao {

	protected UserDaoImpl() {
		super(User_.class);
	}

}
