package com.walmart.app.ws.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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
import com.walmart.app.ws.service.AddressService;
import com.walmart.app.ws.service.UserService;
import com.walmart.app.ws.shared.dto.AddressDTO;
import com.walmart.app.ws.shared.dto.UserDto;
import com.walmart.app.ws.ui.model.request.PasswordResetRequestModel;
import com.walmart.app.ws.ui.model.request.UserDetailsRequestModel;
import com.walmart.app.ws.ui.model.request.PasswordResetModel;
import com.walmart.app.ws.ui.model.response.AddressesRest;
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
	
	@Autowired
	AddressService addressesService;

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
		
		//UserDto userDto = new UserDto();
		//BeanUtils.copyProperties(userDetails, userDto);
		//Below code is better for mapping deeply nested object
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		
		UserDto createdUser = userService.createUser(userDto);
		retVal = modelMapper.map(createdUser, UserRest.class);
		
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

	/***************************************************************
	 * GET USER ADDRESSES
	 * http://localhost:8080/mobile-app-ws/users/<userId>/addresses
	 ***************************************************************/
	@GetMapping(
			path="/{id}/addresses",
			produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public CollectionModel<AddressesRest> getUserAddresses(@PathVariable String id) {
		List<AddressesRest> retVal = new ArrayList<>();

		List<AddressDTO> addressesDTO = addressesService.getAddresses(id);

		if (addressesDTO != null && !addressesDTO.isEmpty()) {
			Type listType = new TypeToken<List<AddressesRest>>() {}.getType();
			retVal = new ModelMapper().map(addressesDTO, listType);
			
			// Add Embedded Links
			for (AddressesRest addressRest : retVal) {
				Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
						.methodOn(UserController.class)
						.getUserAddress(id, addressRest.getAddressId()))
						.withSelfRel();
				addressRest.add(selfLink);
			}
		}
		
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(id).withRel("user");
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
				.methodOn(UserController.class)
				.getUserAddresses(id))
				.withSelfRel();
		
		return CollectionModel.of(retVal, userLink, selfLink);
	}

	/***************************************
	 * GET USER ADDRESS
	 * Adding Links.. two methods
	 * 	- Representation Model (Lecture 120)
	 * 	- Entity Model (Lecture 121)
	 ***************************************/
	@GetMapping(
			path="/{userId}/addresses/{addressId}",
			produces={ MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	public AddressesRest getUserAddress(@PathVariable String userId, @PathVariable String addressId) {
		AddressDTO addressDTO = addressesService.getAddress(addressId);

		AddressesRest retVal = new ModelMapper().map(addressDTO, AddressesRest.class);
		
		// http://localhost:8080/users/<userId>
		Link userLink = WebMvcLinkBuilder.linkTo(UserController.class).slash(userId).withRel("user");
		// http://localhost:8080/users/<userId>/addresses
		Link userAddressesLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
				.methodOn(UserController.class)
				.getUserAddresses(userId))
				//.slash(userId)
				//.slash("addresses")
				.withRel("addresses");
		// http://localhost:8080/users/<userId>/addresses/<addressId>
		Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
				.methodOn(UserController.class)
				.getUserAddress(userId, addressId))
				//.slash(userId)
				//.slash("addresses")
				//.slash(addressId)
				.withSelfRel();
		
		retVal.add(userLink);
		retVal.add(userAddressesLink);
		retVal.add(selfLink);
		return retVal;
		
		/* Alternative method for adding links... This method's return type 
		 * will be EntityModel<AddressRest> and the AddressRest class will no
		 * longer extend RepresentationModel
		 */
		//return EntityModel.of(retVal, Arrays.asList(userLink, userAddressesLink, selfLink));
	}
	
	/****************************************************************************
	 * Email Verification
	 * 
	 * http://localhost:8080/mobile-app-ws/users/email-verification?token=foobar
	 ****************************************************************************/
	@GetMapping(
			path="/email-verification",
			produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
	)
	public OperationStatusModel verifyEmailToken(
			@RequestParam(value="token") String token
	) {
		OperationStatusModel retVal = new OperationStatusModel();
		retVal.setOperationName(RequestOperationName.VERIFY_EMAIL.name());
		boolean isVerified = userService.verifyEmailToken(token);
		retVal.setOperationResult( isVerified
				? RequestOperationStatus.SUCCESS.name()
				: RequestOperationStatus.ERROR.name()
		);
		return retVal;    
	}
	
	/****************************************************************************
	 * Password Reset Request
	 * 
	 * http://localhost:8080/mobile-app-ws/users/password-reset-request
	 ****************************************************************************/
	@PostMapping(
			path = "/password-reset-request",
			produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
			consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public OperationStatusModel requestReset(@RequestBody PasswordResetRequestModel passwordResetRequestModel) {
		OperationStatusModel retVal = new OperationStatusModel();
		
		boolean operationResult = userService.requestPasswordReset(passwordResetRequestModel.getEmail());
		
		retVal.setOperationName(RequestOperationName.REQUEST_PASSWORD_RESET.name());
		retVal.setOperationResult( operationResult
				? RequestOperationStatus.SUCCESS.name()
				: RequestOperationStatus.ERROR.name());

		return retVal;
	}
	
	/****************************************************************************
	 * Password Reset
	 * 
	 * http://localhost:8080/mobile-app-ws/users/password-reset
	 ****************************************************************************/
	@PostMapping(
			path = "/password-reset",
			consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public OperationStatusModel resetPassword(@RequestBody PasswordResetModel passwordResetModel) {
		OperationStatusModel retVal = new OperationStatusModel();
		
		boolean operationResult = userService.resetPassword(
				passwordResetModel.getToken(),
				passwordResetModel.getPassword());
		
		retVal.setOperationName(RequestOperationName.PASSWORD_RESET.name());
		retVal.setOperationResult(RequestOperationStatus.ERROR.name());
		
		if (operationResult) {
			retVal.setOperationResult(RequestOperationStatus.SUCCESS.name());
		}
		return retVal;
	}
}
