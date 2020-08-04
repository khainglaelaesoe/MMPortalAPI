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
}
