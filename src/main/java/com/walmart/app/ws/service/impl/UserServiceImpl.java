package com.walmart.app.ws.service.impl;

import java.util.ArrayList;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.walmart.app.ws.io.entity.UserEntity;
import com.walmart.app.ws.io.repositories.UserRepository;
import com.walmart.app.ws.service.UserService;
import com.walmart.app.ws.shared.Utils;
import com.walmart.app.ws.shared.dto.UserDto;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	Utils utils;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Override
	public UserDto createUser(UserDto user) {
		
		if (userRepository.findByEmail(user.getEmail()) != null) {
			throw new RuntimeException("Record already exists.");
		}
		
		
		UserEntity userEntity = new UserEntity();
		BeanUtils.copyProperties(user, userEntity);
		
		String publicUserId = utils.generateUserId(30);
		userEntity.setUserId(publicUserId);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		
		UserEntity storedUserDetails = userRepository.save(userEntity);
		
		UserDto retVal = new UserDto();
		BeanUtils.copyProperties(storedUserDetails, retVal);
		
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

	/********************
	 * username == email
	 ********************/
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserDto retVal = new UserDto();
		UserEntity userEntity = userRepository.findByUserId(userId);
		
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);
		
		BeanUtils.copyProperties(userEntity, retVal);
		
		return retVal;
	}
}
