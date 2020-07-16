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
	
	public String generateEmailVerificationToken(String userId) {
		String token = Jwts.builder()
				.setSubject(userId)
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SecurityConstants.getTokenSecret())
				.compact();
		return token;
	}
}