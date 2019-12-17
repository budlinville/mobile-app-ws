package com.walmart.app.ws.ui.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walmart.app.ws.exceptions.UserServiceException;
import com.walmart.app.ws.service.UserService;
import com.walmart.app.ws.shared.dto.UserDto;
import com.walmart.app.ws.ui.model.request.UserDetailsRequestModel;
import com.walmart.app.ws.ui.model.response.ErrorMessages;
import com.walmart.app.ws.ui.model.response.OperationStatusModel;
import com.walmart.app.ws.ui.model.response.RequestOperationName;
import com.walmart.app.ws.ui.model.response.RequestOperationStatus;
import com.walmart.app.ws.ui.model.response.UserRest;

@RestController
@RequestMapping("users")	// http://localhost:8080/users
public class UserController {
	
	@Autowired
	UserService userService;
	
	/**************
	 * GET USER
	 **************/
	@GetMapping(
			path="/{id}", 
			produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public UserRest getUser(@PathVariable String id) {
		UserRest retVal = new UserRest();
		
		UserDto userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, retVal);
		
		return retVal;
	}
	
	/**************
	 * CREATE USER
	 **************/
	@PostMapping(
			consumes={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public UserRest createUser(@RequestBody UserDetailsRequestModel userDetails) throws UserServiceException {
		UserRest retVal = new UserRest();
		
		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);
		
		UserDto createdUser = userService.createUser(userDto);
		BeanUtils.copyProperties(createdUser, retVal);
		
		return retVal;
	}
	
	/**************
	 * UPDATE USER
	 **************/
	@PutMapping(
			path="/{id}",
			consumes={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public UserRest updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserRest retVal = new UserRest();
		
		if (userDetails.getFirstName().isEmpty())
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		
		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);
		
		UserDto updatedUser = userService.updateUser(id, userDto);
		BeanUtils.copyProperties(updatedUser, retVal);
		
		return retVal;
	}
	
	/**************
	 * DELETE USER
	 **************/
	@DeleteMapping(
			path="/{id}",
			produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public OperationStatusModel deleteUser(@PathVariable String id) {
		OperationStatusModel retVal = new OperationStatusModel();
		
		retVal.setOperationName(RequestOperationName.DELETE.name());
		
		userService.deleteUser(id);
		
		retVal.setOperationResult(RequestOperationStatus.SUCCESS.name());
		
		return retVal;
	}
	
	@GetMapping( produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE } )
	public List<UserRest> getUsers(
			@RequestParam(value="page", defaultValue="1") int page,
			@RequestParam(value="limit", defaultValue="25") int limit
	) {
		List<UserRest> retVal = new ArrayList<UserRest>();
		
		if (page>0)
			page = page - 1;
		
		List<UserDto> users = userService.getUsers(page, limit);
		
		for (UserDto userDto : users) {
			UserRest userModel = new UserRest();
			BeanUtils.copyProperties(userDto, userModel);
			retVal.add(userModel);
		}
		
		return retVal;
	}
}
