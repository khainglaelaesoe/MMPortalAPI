package com.portal.serive.impl;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.OrganizationDao;
import com.portal.entity.Organization_;
import com.portal.service.OrganizationService;

@Service("organizationService")
public class OrganizationServiceImpl implements OrganizationService {

	@Autowired
	private OrganizationDao organizationDao;

	@Override
	public List<Organization_> getAll() {
		String query = "from Organization_ org";
		return organizationDao.getAll(query);
	}

	@Override
	public void hibernateInitialize(Organization_ organization) {
		Hibernate.initialize(organization.getMyanmarName());
		Hibernate.initialize(organization.getEngName());
	}

	@Override
	public void hibernateInitializeOrganizationList(List<Organization_> organizationList) {
		organizationList.forEach(organization -> {
			hibernateInitialize(organization);
		});
	}
}
