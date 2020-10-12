package com.portal.service;

import java.security.NoSuchAlgorithmException;

import com.portal.entity.Result;
import com.portal.entity.mobileuser;
import java.security.spec.InvalidKeySpecException;

public interface MobileUserService {

	public Result saveUser(mobileuser user);

	public void save(mobileuser user);

	public String hashPassword(String password, boolean liferayCompatible, String encryptedPassword);

	public boolean validatePassword(String password, String hash) throws NoSuchAlgorithmException, InvalidKeySpecException;

	public Result update(mobileuser user);

	public String selectPwd(long id);

	public String getEntrypedPasswordByEmail(String email);

	public mobileuser getUserByEmail(String email);

	public mobileuser getUserByPhone(String phoneno);

	public mobileuser validUserid(long userid);

	public mobileuser getUserByUserId(Long userId);	
}
