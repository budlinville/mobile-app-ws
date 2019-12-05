package com.walmart.app.ws.exceptions;

import java.util.Date;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.walmart.app.ws.ui.model.response.ErrorMessage;

@ControllerAdvice
public class AppExceptionsHandler {
	
	@ExceptionHandler(value = {UserServiceException.class})	//Can add more exceptions here
	public ResponseEntity<Object> handleUserServiceException(UserServiceException ex, WebRequest request) {
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/************************************************************************************
	 * EXCEPTION HANDLER FOR ALL OTHER EXCEPTIONS BESIDES THOSE EXPLICITLY HANDLED ABOVE
	 * @param ex
	 * @param request
	 * @return
	 ************************************************************************************/
	@ExceptionHandler(value = {Exception.class})
	public ResponseEntity<Object> handleOtherExceptions(Exception ex, WebRequest request) {
		ErrorMessage errorMessage = new ErrorMessage(new Date(), ex.getMessage());
		
		return new ResponseEntity<>(errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
