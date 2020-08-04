package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.MobileUserDao;
import com.portal.entity.mobileuser;

@Repository
public class MobileUserDaoImpl extends AbstractDaoImpl<mobileuser, String> implements MobileUserDao {

	protected MobileUserDaoImpl() {
		super(mobileuser.class);
	}
}