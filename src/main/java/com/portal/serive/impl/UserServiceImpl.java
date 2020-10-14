package com.portal.serive.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
		User_ res = new User_();
		String query = "from User_ where emailaddress='" + emailaddress + "'";
		List<User_> users = userDao.getAll(query);
		if (users.size() > 0) {
			res = userDao.getAll(query).get(0);
			res.setPhone(getPhone(res.getUserid()));
			return res;
		} else
			return null;
	}

	public String getPhone(long userid) {
		String phoneNo = "";
		String query = "Select number_ from Phone where userid=" + userid + " order by phoneId desc ";
		List<String> strList = userDao.findByQuery(query);
		if (strList.size() > 0) {
			phoneNo = strList.get(0);
		}
		return phoneNo;
	}

	public User_ getUserbyfacebookID(String facebookID) {
		String query = "from User_ where facebookId=" + facebookID;

		List<User_> users = userDao.getAll(query);
		if (users.size() > 0)
			return userDao.getAll(query).get(0);
		else
			return null;
	}

	public Long getIdByEmail(String emailAddress) {
		String query = "select userid from User_ u where emailAddress='" + emailAddress + "'";
		List<Long> idList = userDao.findLongByQueryString(query);
		if (CollectionUtils.isEmpty(idList))
			return null;
		return idList.get(0);
	}

	public User_ getMNPUserByEmail(String email) {
		String query = "from User_ where emailaddress='" + email + "'";
		List<User_> userList = userDao.getAll(query);
		if (CollectionUtils.isEmpty(userList))
			return null;
		return userList.get(0);
	}
	
	public User_ getMNPUserByUserId(String userId) {
		String query = "from User_ where userid=" + userId;
		List<User_> userList = userDao.getAll(query);
		if (CollectionUtils.isEmpty(userList))
			return null;
		return userList.get(0);
	}
	
	public String getPhoneByUserId(String userId) {
		String query = "select number_ from Phone where userid=" + userId;
		List<String> phoneList = userDao.findByQuery(query);
		if (CollectionUtils.isEmpty(phoneList))
			return null;
		return phoneList.get(0);
	}

	
	public User_ getScreenName(String screenname) {
		User_ res = new User_();
		String query = "from User_ where screenname='"+ screenname +"'";
		List<User_> users = userDao.getAll(query);
		if(users.size() > 0) {
			res = userDao.getAll(query).get(0);
			res.setPhone(getPhone(res.getUserid()));
			return res;
		}else return null;
	}
}
