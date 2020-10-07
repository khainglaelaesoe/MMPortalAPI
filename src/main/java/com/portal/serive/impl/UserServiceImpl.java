package com.portal.serive.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.portal.dao.UserDao;
import com.portal.entity.User_;
import com.portal.service.UserService;

@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	public List<User_> getAllWebUsers() {
		String query = "select u from User_ u";
		List<User_> users = userDao.getAll(query);
		return users;
	}
	
	public User_ getUserbyemail(String emailaddress) {
		String query = "from User_ where emailaddress='"+ emailaddress +"'";
		List<User_> users = userDao.getAll(query);
		if(users.size() > 0) 
			 return userDao.getAll(query).get(0);
		else return null;
	}
	
	public User_ getUserbyfacebookID(String facebookID) {
		String query = "from User_ where facebookId="+ facebookID;
		List<User_> users = userDao.getAll(query);
		if(users.size() > 0) 
			 return userDao.getAll(query).get(0);
		else return null;
	}
}
