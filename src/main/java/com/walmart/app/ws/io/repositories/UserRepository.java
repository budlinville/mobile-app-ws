package com.walmart.app.ws.io.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.walmart.app.ws.io.entity.UserEntity;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
	// CRUD operations get auto-generated?
	
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
}
