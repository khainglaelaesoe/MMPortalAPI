package com.portal.entity;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractEntity {

	public boolean isBoIdRequired(Long boId) {
		return boId == null || SystemConstant.BOID_REQUIRED.equals(boId);
	}

}
