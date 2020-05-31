package com.walmart.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.walmart.app.ws.io.entity.AddressEntity;
import com.walmart.app.ws.io.entity.UserEntity;
import com.walmart.app.ws.io.repositories.AddressRepository;
import com.walmart.app.ws.io.repositories.UserRepository;
import com.walmart.app.ws.service.AddressService;
import com.walmart.app.ws.shared.dto.AddressDTO;

@Service
public class AddressServiceImpl implements AddressService {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AddressRepository addressRepository;
	
	@Override
	public List<AddressDTO> getAddresses(String userId) {
		List<AddressDTO> retVal = new ArrayList<>();
		ModelMapper modelMapper = new ModelMapper();
		
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity==null) return retVal;

		Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);
		
		for (AddressEntity addressEntity : addresses) {
			retVal.add(modelMapper.map(addressEntity, AddressDTO.class));
		}
		
		return retVal;
	}

	@Override
	public AddressDTO getAddress(String addressId) {
		AddressDTO retVal = null;
		
		AddressEntity addressEntity = addressRepository.findByAddressId(addressId);
		
		if (addressEntity != null) {
			retVal = new ModelMapper().map(addressEntity, AddressDTO.class);
		}
		
		return retVal;
	}

}
