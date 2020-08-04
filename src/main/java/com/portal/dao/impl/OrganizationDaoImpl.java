package com.portal.dao.impl;

import org.springframework.stereotype.Repository;

import com.portal.dao.OrganizationDao;
import com.portal.entity.Organization_;

@Repository
public class OrganizationDaoImpl extends AbstractDaoImpl<Organization_, String> implements OrganizationDao {

	protected OrganizationDaoImpl() {
		super(Organization_.class);
	}

}
