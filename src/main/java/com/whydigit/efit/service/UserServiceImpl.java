package com.whydigit.efit.service;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.whydigit.efit.common.CommonConstant;
import com.whydigit.efit.common.UserConstants;
import com.whydigit.efit.dto.ChangePasswordFormDTO;
import com.whydigit.efit.dto.LoginFormDTO;
import com.whydigit.efit.dto.RefreshTokenDTO;
import com.whydigit.efit.dto.ResetPasswordFormDTO;
import com.whydigit.efit.dto.Role;
import com.whydigit.efit.dto.SignUpFormDTO;
import com.whydigit.efit.dto.UserResponseDTO;
import com.whydigit.efit.entity.CheckinStatusVO;
import com.whydigit.efit.entity.TokenVO;
import com.whydigit.efit.entity.UserActionVO;
import com.whydigit.efit.entity.UserVO;
import com.whydigit.efit.exception.ApplicationException;
import com.whydigit.efit.repo.CheckinStatusRepo;
import com.whydigit.efit.repo.TokenRepo;
import com.whydigit.efit.repo.UserActionRepo;
import com.whydigit.efit.repo.UserRepo;
import com.whydigit.efit.security.TokenProvider;
import com.whydigit.efit.util.CryptoUtils;

@Service
public class UserServiceImpl implements UserService {
	public static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	UserRepo userRepo;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	UserActionRepo userActionRepo;

	@Autowired
	TokenProvider tokenProvider;

	@Autowired
	TokenRepo tokenRepo;
	
	@Autowired
	CheckinStatusRepo chkstatusRepo;

	@Override
	public void signup(SignUpFormDTO signUpRequest) {
		String methodName = "signup()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (ObjectUtils.isEmpty(signUpRequest) || StringUtils.isBlank(signUpRequest.getEmail())
				|| StringUtils.isBlank(signUpRequest.getEmail())) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_USER_REGISTER_INFORMATION);
		} else if (userRepo.existsByEmail(signUpRequest.getEmail())) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_ALREADY_REGISTERED);
		}
		UserVO userVO = getUserVOFromSignUpFormDTO(signUpRequest);
		userRepo.save(userVO);
		CheckinStatusVO statusvo=new CheckinStatusVO();
		statusvo.setEmpcode(userVO.getEmpcode());
		statusvo.setStatus("Out");
		chkstatusRepo.save(statusvo);
		createUserAction(userVO.getEmail(), userVO.getUserId(), UserConstants.USER_ACTION_ADD_ACCOUNT);
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
	}

	private UserVO getUserVOFromSignUpFormDTO(SignUpFormDTO signUpRequest) {
		UserVO userVO = new UserVO();
		userVO.setEmpcode(signUpRequest.getEmpcode());
		userVO.setEmpname(signUpRequest.getEmpname());
		userVO.setEmail(signUpRequest.getEmail());
		try {
			userVO.setPassword(encoder.encode(CryptoUtils.getDecrypt(signUpRequest.getPassword())));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_UNABLE_TO_ENCODE_USER_PASSWORD);
		}
		userVO.setRole(Role.USER);
		userVO.setActive(true);
		return userVO;
	}

	@Override
	public UserResponseDTO login(LoginFormDTO loginRequest) {
		String methodName = "login()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (ObjectUtils.isEmpty(loginRequest) || StringUtils.isBlank(loginRequest.getEmail())
				|| StringUtils.isBlank(loginRequest.getPassword())) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_USER_LOGIN_INFORMATION);
		}
		UserVO userVO = userRepo.findByEmail(loginRequest.getEmail());
		if (ObjectUtils.isNotEmpty(userVO)) {
			if (compareEncodedPasswordWithEncryptedPassword(loginRequest.getPassword(), userVO.getPassword())) {
				updateUserLoginInformation(userVO);
			} else {
				throw new ApplicationContextException(UserConstants.ERRROR_MSG_PASSWORD_MISMATCH);
			}
		} else {
			throw new ApplicationContextException(
					UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND_AND_ASKING_SIGNUP);
		}
		UserResponseDTO userResponseDTO = mapUserVOToDTO(userVO);
		TokenVO tokenVO = tokenProvider.createToken(userVO.getUserId(), loginRequest.getEmail());
		userResponseDTO.setToken(tokenVO.getToken());
		userResponseDTO.setTokenId(tokenVO.getId());
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
		return userResponseDTO;
	}

	/**
	 * @param encryptedPassword -> Data from user;
	 * @param encodedPassword   ->Data from DB;
	 * @return
	 */
	private boolean compareEncodedPasswordWithEncryptedPassword(String encryptedPassword, String encodedPassword) {
		boolean userStatus = false;
		try {
			userStatus = encoder.matches(CryptoUtils.getDecrypt(encryptedPassword), encodedPassword);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_UNABLE_TO_ENCODE_USER_PASSWORD);
		}
		return userStatus;
	}

	/**
	 * @param userVO
	 */
	private void updateUserLoginInformation(UserVO userVO) {
		try {
			userVO.setLoginStatus(true);
			userRepo.save(userVO);
			createUserAction(userVO.getEmail(), userVO.getUserId(), UserConstants.USER_ACTION_TYPE_LOGIN);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_UNABLE_TO_UPDATE_USER_INFORMATION);
		}
	}

	@Override
	public void createUserAction(String email, long userId, String actionType) {
		try {
			UserActionVO userActionVO = new UserActionVO();
			userActionVO.setEmail(email);
			userActionVO.setUserId(userId);
			userActionVO.setActionType(actionType);
			userActionRepo.save(userActionVO);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}

	@Override
	public void logout(String email) {
		String methodName = "logout()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (StringUtils.isBlank(email)) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_USER_LOGOUT_INFORMATION);
		}
		UserVO userVO = userRepo.findByEmail(email);
		if (ObjectUtils.isNotEmpty(userVO)) {
			updateUserLogOutInformation(userVO);
		} else {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
	}

	private void updateUserLogOutInformation(UserVO userVO) {
		try {
			userVO.setLoginStatus(false);
			userRepo.save(userVO);
			createUserAction(userVO.getEmail(), userVO.getUserId(), UserConstants.USER_ACTION_TYPE_LOGOUT);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_UNABLE_TO_UPDATE_USER_INFORMATION);
		}
	}

	@Override
	public void changePassword(ChangePasswordFormDTO changePasswordRequest) {
		String methodName = "changePassword()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (ObjectUtils.isEmpty(changePasswordRequest) || StringUtils.isBlank(changePasswordRequest.getEmail())
				|| StringUtils.isBlank(changePasswordRequest.getOldPassword())
				|| StringUtils.isBlank(changePasswordRequest.getNewPassword())) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_CHANGE_PASSWORD_INFORMATION);
		}
		UserVO userVO = userRepo.findByEmail(changePasswordRequest.getEmail());
		if (ObjectUtils.isNotEmpty(userVO)) {
			if (compareEncodedPasswordWithEncryptedPassword(changePasswordRequest.getOldPassword(),
					userVO.getPassword())) {
				try {
					userVO.setPassword(encoder.encode(CryptoUtils.getDecrypt(changePasswordRequest.getNewPassword())));
				} catch (Exception e) {
					throw new ApplicationContextException(UserConstants.ERRROR_MSG_UNABLE_TO_ENCODE_USER_PASSWORD);
				}
				userRepo.save(userVO);
				createUserAction(userVO.getEmail(), userVO.getUserId(),
						UserConstants.USER_ACTION_TYPE_CHANGE_PASSWORD);
			} else {
				throw new ApplicationContextException(UserConstants.ERRROR_MSG_OLD_PASSWORD_MISMATCH);
			}
		} else {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
	}

	@Override
	public void resetPassword(ResetPasswordFormDTO resetPasswordRequest) {
		String methodName = "resetPassword()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (ObjectUtils.isEmpty(resetPasswordRequest) || StringUtils.isBlank(resetPasswordRequest.getEmail())
				|| StringUtils.isBlank(resetPasswordRequest.getNewPassword())) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_RESET_PASSWORD_INFORMATION);
		}
		UserVO userVO = userRepo.findByEmail(resetPasswordRequest.getEmail());
		if (ObjectUtils.isNotEmpty(userVO)) {
			try {
				userVO.setPassword(encoder.encode(CryptoUtils.getDecrypt(resetPasswordRequest.getNewPassword())));
			} catch (Exception e) {
				throw new ApplicationContextException(UserConstants.ERRROR_MSG_UNABLE_TO_ENCODE_USER_PASSWORD);
			}
			userRepo.save(userVO);
			createUserAction(userVO.getEmail(), userVO.getUserId(), UserConstants.USER_ACTION_TYPE_RESET_PASSWORD);
		} else {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
	}

	@Override
	public UserVO getUserById(Long userId) {
		String methodName = "getUserById()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (ObjectUtils.isEmpty(userId)) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_USER_ID);
		}
		UserVO userVO = userRepo.getUserById(userId);
		if (ObjectUtils.isEmpty(userVO)) {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
		}
		LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
		return userVO;
	}

	@Override
	public UserVO getUserByUserName(String email) {
		String methodName = "getUserByEmail()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (StringUtils.isNotEmpty(email)) {
			UserVO userVO = userRepo.findByEmail(email);
			if (ObjectUtils.isEmpty(userVO)) {
				throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
			}
			LOGGER.debug(CommonConstant.ENDING_METHOD, methodName);
			return userVO;
		} else {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_USER_NAME);
		}
	}

	@Override
	public void removeUser(String email) {
		String methodName = "removeUser()";
		LOGGER.debug(CommonConstant.STARTING_METHOD, methodName);
		if (StringUtils.isNotEmpty(email)) {
			UserVO userVO = userRepo.findByEmail(email);
			if (ObjectUtils.isEmpty(userVO)) {
				throw new ApplicationContextException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
			}
			userVO.setActive(false);
			userVO.setAccountRemovedDate(new Date());
			userRepo.save(userVO);
			createUserAction(userVO.getEmail(), userVO.getUserId(), UserConstants.USER_ACTION_REMOVE_ACCOUNT);
		} else {
			throw new ApplicationContextException(UserConstants.ERRROR_MSG_INVALID_USER_NAME);
		}
	}

	public static UserResponseDTO mapUserVOToDTO(UserVO userVO) {
		UserResponseDTO userDTO = new UserResponseDTO();
		userDTO.setUserId(userVO.getUserId());
		userDTO.setEmpcode(userVO.getEmpcode());
		userDTO.setEmpname(userVO.getEmpname());
		userDTO.setEmail(userVO.getEmail());
		userDTO.setLoginStatus(userVO.isLoginStatus());
		userDTO.setActive(userVO.isActive());
		userDTO.setRole(userVO.getRole());
		userDTO.setCommonDate(userVO.getCommonDate());
		userDTO.setAccountRemovedDate(userVO.getAccountRemovedDate());
		return userDTO;
	}

	@Override
	public RefreshTokenDTO getRefreshToken(String email, String tokenId) throws ApplicationException {
		UserVO userVO = userRepo.findByEmail(email);
		RefreshTokenDTO refreshTokenDTO = null;
		if (ObjectUtils.isEmpty(userVO)) {
			throw new ApplicationException(UserConstants.ERRROR_MSG_USER_INFORMATION_NOT_FOUND);
		}
		TokenVO tokenVO = tokenRepo.findById(tokenId).orElseThrow(() -> new ApplicationException("Invalid Token Id."));
		if (tokenVO.getExpDate().compareTo(new Date()) > 0) {
			tokenVO = tokenProvider.createRefreshToken(tokenVO, userVO);
			refreshTokenDTO = RefreshTokenDTO.builder().token(tokenVO.getToken()).tokenId(tokenVO.getId()).build();
		} else {
			tokenRepo.delete(tokenVO);
			throw new ApplicationException(UserConstants.REFRESH_TOKEN_EXPIRED_MESSAGE);
		}
		return refreshTokenDTO;
	}
}
