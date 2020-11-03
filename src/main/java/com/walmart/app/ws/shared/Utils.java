package com.walmart.app.ws.shared;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.walmart.app.ws.security.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class Utils {
	private final Random RANDOM = new SecureRandom();
	private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
	public String generateUserId(int length) {
		return generateRandomString(length);
	}
	
	public String generateAddressId(int length) {
		return generateRandomString(length);
	}

	private String generateRandomString(int length) {
		StringBuilder retVal = new StringBuilder(length);
		
		for (int i = 0; i < length; i++) {
			retVal.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
		}
		
		return new String(retVal);
	}
	
	public static boolean hasTokenExpired(String token) {
		Claims claims = Jwts.parser()
				.setSigningKey( SecurityConstants.getTokenSecret() )
				.parseClaimsJws( token )
				.getBody();
		
		Date tokenExpDate = claims.getExpiration();
		Date today = new Date();
		
		return tokenExpDate.before(today);
	}
	
	private String generateToken(String id, long lifetime) {
		return Jwts.builder()
			.setSubject(id)
			.setExpiration(new Date(System.currentTimeMillis() + lifetime))
			.signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret())
			.compact();
	}
	
	public String generateEmailVerificationToken(String userId) {
		return generateToken(userId, SecurityConstants.EXPIRATION_TIME);
	}
	
	public String generatePasswordResetToken(String userId) {
		return generateToken(userId, SecurityConstants.PASSWORD_RESET_EXPIRATION_TIME);
	}
}