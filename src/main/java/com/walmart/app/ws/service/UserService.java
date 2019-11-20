package com.walmart.app.ws.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.walmart.app.ws.shared.dto.UserDto;

public interface UserService extends UserDetailsService {
	UserDto createUser(UserDto user);
}