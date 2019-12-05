package com.walmart.app.ws.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.walmart.app.ws.shared.dto.UserDto;

public interface UserService extends UserDetailsService {
	UserDto createUser(UserDto user);
	UserDetails loadUserByUsername(String email);
	UserDto getUser(String email);
	UserDto getUserByUserId(String userId);
}