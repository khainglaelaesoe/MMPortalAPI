package com.portal.service;

import java.util.List;

import com.portal.entity.User_;

public interface UserService {
	public List<User_> getAllWebUsers();

	public User_ getUserbyemail(String emailaddress);
	
	public User_ getUserbyfacebookID(String facebookID);

	public Long getIdByEmail(String emailAddress);

}
