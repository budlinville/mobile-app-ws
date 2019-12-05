package com.walmart.app.ws.exceptions;

public class UserServiceException extends RuntimeException {

	private static final long serialVersionUID = 3144700439982132119L;

	public UserServiceException(String message) {
		super(message);
	}
}
