package com.portal.serive.impl;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.mchange.rmi.ServiceUnavailableException;
import com.portal.dao.MobileUserDao;
import com.portal.encryption.PBKDF2PasswordEncryptor;
import com.portal.entity.Result;
import com.portal.entity.User_;
import com.portal.entity.mobileuser;
import com.portal.service.MobileUserService;

@Service("mobileUserService")
public class MobileUserServiceImpl implements MobileUserService {

	@Autowired
	private MobileUserDao mobileUserDao;

	private final static int MAX_SCREEN_NAME_LENGTH = 26;

	private static Logger logger = Logger.getLogger(MobileUserServiceImpl.class);

	public Result saveUser(mobileuser user) {
		Result res = new Result();
		try {
			if (user.isBoIdRequired(user.getUserid()))
				user.setUserid(getUserId());
			boolean correct = mobileUserDao.checkSaveOrUpdate(user);
			if (correct)
				res.setDescription("Successfully");
			else
				res.setDescription("Fail");

		} catch (ServiceUnavailableException e) {
			logger.error("Error: " + e);
		}
		return res;
	}

	public mobileuser validUserid(long userid) {
		String query = "select u from mobileuser u where userid =" + userid;
		List<mobileuser> userlist = mobileUserDao.findById(query);
		if(CollectionUtils.isEmpty(userlist))
			return null;
		return userlist.get(0);
	}

	public Result update(mobileuser user) {
		Result res = new Result();
		try {
			user.setModifieddate(new Date());
			boolean correct = mobileUserDao.saveUpdate(user);
			if (correct)
				res.setDescription("Successfully");
			else
				res.setDescription("Fail");

		} catch (ServiceUnavailableException e) {
			logger.error("Error: " + e);
		}
		return res;
	}

	public String selectPwd(long id) {
		String respwd = "";
		String query = "select password from mobileuser where userid=" + id;
		respwd = mobileUserDao.validpwd(query);
		return respwd;
	}

	public void save(mobileuser user) {
		try {
			if (user.isBoIdRequired(user.getUserid()))
				user.setUserid(getUserId());
			mobileUserDao.saveOrUpdate(user);
		} catch (ServiceUnavailableException e) {
			logger.error("Error: " + e);
		}
	}

	private Long getUserId() {
		return countUser() + 1;
	}

	public long countUser() {
		String query = "select count(*) from mobileuser";
		return mobileUserDao.findLongByQueryString(query).get(0);
	}

	public String hashPassword(String password, boolean liferayCompatible, String encryptedPassword) {
		try {
			PBKDF2PasswordEncryptor pbkdf2PasswordEncryptor = new PBKDF2PasswordEncryptor();
			return "PBKDF2_" + pbkdf2PasswordEncryptor.doEncrypt(PBKDF2PasswordEncryptor.DEFAULT_ALGORITHM, password, encryptedPassword);
		} catch (Exception e) {
			logger.error("Error: " + e);
		}
		return "";
	}
//
//	public boolean validatePassword(String password, String hash) {
//		try {
//			PBKDF2PasswordEncryptor pbkdf2PasswordEncryptor = new PBKDF2PasswordEncryptor();
//			final String unprefixedHash = hash.substring(7);
//
//			return pbkdf2PasswordEncryptor.doEncrypt(PBKDF2PasswordEncryptor.DEFAULT_ALGORITHM, password, unprefixedHash).equals(unprefixedHash);
//		} catch (Exception e) {
//			logger.error("Error: " + e);
//		}
//		return false;
//	}

	public boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
		String[] parts = storedPassword.split("_");
		int keysize = Integer.parseInt(parts[1]);
		int iterations = Integer.parseInt(parts[2]);
		// byte[] saltdecoded = Base64.decodeBase64(parts[3]);
		// String hexSaltString = Hex.encodeHexString(saltdecoded);
		byte[] salt = fromHex(parts[3]);
		// byte[] secretdecoded = Base64.decodeBase64(parts[4]);
		// String hexSecretString = Hex.encodeHexString(secretdecoded);
		byte[] hash = fromHex(parts[4]);

		PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, keysize);

		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

		byte[] testHash = skf.generateSecret(spec).getEncoded();

		int diff = hash.length ^ testHash.length;

		for (int i = 0; i < hash.length && i < testHash.length; i++) {
			diff |= hash[i] ^ testHash[i];
		}

		return diff == 0;
	}

	private static byte[] fromHex(String hex) throws NoSuchAlgorithmException {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}

	public String getEntrypedPasswordByEmail(String email) {
		String query = "select password from mobileuser where emailaddress='" + email + "'";
		return mobileUserDao.findByQuery(query).get(0);
	}

	public mobileuser getUserByEmail(String email) {
		String query = "from mobileuser where emailaddress='" + email + "'";
		List<mobileuser> mobileUserList = mobileUserDao.getAll(query);
		if (CollectionUtils.isEmpty(mobileUserList))
			return null;
		return mobileUserList.get(0);
	}
	


	public mobileuser getUserByPhone(String phoneno) {
		String query = "from mobileuser where phoneno='" + phoneno + "'";
		List<mobileuser> mobileUserList = mobileUserDao.getAll(query);
		if (CollectionUtils.isEmpty(mobileUserList))
			return null;
		return mobileUserList.get(0);
	}

	public mobileuser getUserByUserId(Long userId) {
		String query = "from mobileuser where userid=" + userId;
		List<mobileuser> mobileUserList = mobileUserDao.getAll(query);
		if (CollectionUtils.isEmpty(mobileUserList))
			return null;
		return mobileUserList.get(0);

	}
}