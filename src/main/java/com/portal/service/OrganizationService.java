package com.portal.service;

import java.util.List;

import com.portal.entity.Organization_;

public interface OrganizationService {

	public List<Organization_> getAll();

	public void hibernateInitialize(Organization_ organization);

	public void hibernateInitializeOrganizationList(List<Organization_> organizationList);

}
