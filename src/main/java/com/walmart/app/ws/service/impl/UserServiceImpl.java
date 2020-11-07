package com.walmart.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.walmart.app.ws.exceptions.UserServiceException;
import com.walmart.app.ws.io.entity.PasswordResetTokenEntity;
import com.walmart.app.ws.io.entity.UserEntity;
import com.walmart.app.ws.io.repositories.UserRepository;
import com.walmart.app.ws.service.UserService;
import com.walmart.app.ws.shared.AmazonSES;
import com.walmart.app.ws.shared.Utils;
import com.walmart.app.ws.shared.dto.AddressDTO;
import com.walmart.app.ws.shared.dto.UserDto;
import com.walmart.app.ws.ui.model.response.ErrorMessages;
import com.walmart.app.ws.io.repositories.PasswordResetTokenRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	PasswordResetTokenRepository passwordResetTokenRepository;

	@Override
	public UserDto createUser(UserDto user) {
		if (userRepository.findByEmail(user.getEmail()) != null) {
			throw new RuntimeException("Record already exists.");
		}

		for (int i = 0; i < user.getAddresses().size(); i++) {
			AddressDTO address = user.getAddresses().get(i);
			address.setUserDetails(user);
			//address.setAddressId(utils.generateAddressId(30));
			address.setAddressId(UUID.randomUUID().toString());
			user.getAddresses().set(i, address);
		}

		//BeanUtils.copyProperties(user, userEntity);
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);
		
		// String publicUserId = utils.generateUserId(30);
		String publicUserId = UUID.randomUUID().toString();
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setEmailVerificationToken(utils.generateEmailVerificationToken(publicUserId));
		userEntity.setEmailVerificationStatus(false);
		
		UserEntity storedUserDetails = userRepository.save(userEntity);

		//BeanUtils.copyProperties(storedUserDetails, retVal);
		UserDto retVal = modelMapper.map(storedUserDetails, UserDto.class);
		
		// Send an email message to the user to verify their email address
		new AmazonSES().verifyEmail(retVal);
		
		return retVal;
	}

	@Override
	public UserDto getUser(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		UserDto retVal = new UserDto();
		BeanUtils.copyProperties(userEntity, retVal);
		return retVal;
	}

	// username == email
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(),
				userEntity.getEmailVerificationStatus(),
				true, true,
				true, new ArrayList<>());
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserDto retVal = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new UsernameNotFoundException("User with ID: " + userId + " not found.");

		BeanUtils.copyProperties(userEntity, retVal);

		return retVal;
	}

	@Override
	public UserDto updateUser(String userId, UserDto user) {
		UserDto retVal = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());

		// No SQL query... just use 'save'... how great is that
		UserEntity updatedUserDetails = userRepository.save(userEntity);
		BeanUtils.copyProperties(updatedUserDetails, retVal);

		return retVal;
	}

	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);

		if (userEntity == null)
			throw new UsernameNotFoundException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);
	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> retVal = new ArrayList<>();
		
		Pageable pageableRequest = PageRequest.of(page, limit);
		
		Page<UserEntity> usersPage = userRepository.findAll(pageableRequest);
		List<UserEntity> users = usersPage.getContent();
		
		for (UserEntity userEntity : users) {
			UserDto userDto = new UserDto();
			BeanUtils.copyProperties(userEntity, userDto);
			retVal.add(userDto);
		}
		
		return retVal;
	}

	@Override
	public boolean verifyEmailToken(String token) {
		boolean retVal = false;
		
		// Find user by token
		UserEntity userEntity = userRepository.findUserByEmailVerificationToken(token);
		
		if (userEntity != null) {
			boolean tokenExpired = Utils.hasTokenExpired(token);
			if (!tokenExpired) {
				userEntity.setEmailVerificationToken(null);
				userEntity.setEmailVerificationStatus(Boolean.TRUE);
				userRepository.save(userEntity);
				retVal = true;
			}
		}
		return retVal;
	}

	@Override
	public boolean requestPasswordReset(String email) {
		boolean retVal = false;
		
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if (userEntity == null) {
			return retVal;
		}
		
		String token = utils.generatePasswordResetToken(userEntity.getUserId());
		
		PasswordResetTokenEntity passwordResetTokenEntity = new PasswordResetTokenEntity();
		passwordResetTokenEntity.setToken(token);
		passwordResetTokenEntity.setUserDetails(userEntity);
		passwordResetTokenRepository.save(passwordResetTokenEntity);
		
		retVal = new AmazonSES().sendPasswordResetRequest(
				userEntity.getFirstName(),
				userEntity.getEmail(),
				token);
		
		return retVal;
	}

	@Override
	public boolean resetPassword(String token, String password) {
		boolean retVal = false;
		if (Utils.hasTokenExpired(token)) {
			return retVal;
		}
		
		PasswordResetTokenEntity passwordResetTokenEntity = passwordResetTokenRepository.findByToken(token);
		
		if (passwordResetTokenEntity == null) {
			return retVal;
		}
		
		// Prepare new password
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		
		// Update User password in database
		UserEntity userEntity = passwordResetTokenEntity.getUserDetails();
		userEntity.setEncryptedPassword(encodedPassword);
		UserEntity savedUserEntity = userRepository.save(userEntity);
		
		// Verify if password was saved successfully
		if (savedUserEntity != null && savedUserEntity.getEncryptedPassword().equalsIgnoreCase(encodedPassword)) {
			retVal = true;
		}
		
		// Remove Password Reset token from database
		passwordResetTokenRepository.delete(passwordResetTokenEntity);
		
		return retVal;
	 }
}
