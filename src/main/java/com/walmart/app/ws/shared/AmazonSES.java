package com.walmart.app.ws.shared;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.walmart.app.ws.shared.dto.UserDto;

public class AmazonSES {
	// This address must be verified with Amazon SES.
	final String FROM = "budlinville@gmail.com";
	
	// The subject line for the email.
	final String SUBJECT = "One last step to complete your registration.";
	
	final String PASSWORD_RESET_SUBJECT = "Password reset request";
	
	// The HTML body for the email.
	// TODO : Update localhost to production url when deploying in production
	final String HTML_BODY = "<h1>Please verify your email address</h1>"
			+ "<p>Thank you for registering with our mobile app. To complete registration process and be able to log in, "
			+ "please click on the following link: </p>"
			+ "<a href='http://ec2-3-21-104-167.us-east-2.compute.amazonaws.com:8080/verification-service/email-verification.html?token=$tokenValue'>"
			+ "Final step to complete your registration" + "</a><br/><br/>"
			+ "Thanks you! We are waiting for you inside!";
	
	// The email body for recipients with non-HTML email clients
	final String TEXT_BODY = "Please verify your email address"
			+ "Thank you for registering with our mobile app. To complete registration process and be able to access your account, "
			+ "please open the following link in your browser window: "
			+ "http://ec2-3-21-104-167.us-east-2.compute.amazonaws.com:8080/verification-service/email-verification.html?token=$tokenValue'>"
			+ "Thanks you! We are waiting for you inside!";
	
	final String PASSWORD_RESET_HTML_BODY = "<h1>A request to reset your password</h1>"
			+ "<p>Hi, $firstName!</p> "
			+ "<p>Someone has requested to reset your password with our project. If this was not you, please ignore it.</p>"
			+ " Otherwise, please click on the link below to set a new password: "
			+ "<a href='http://localhost:8080/verification-service/password-reset.html?token=$tokenValue'>"
			+ " Click this link to Reset Password"
			+ "</a><br/><br/>"
			+ "Thank you!";
	
	final String PASSWORD_RESET_TEXT_BODY = "A request to reset your password "
			+ "Hi, $firstName! "
			+ "Someone has requested to reset your password with our project. If this was not you, please ignore it."
			+ " Otherwise, please open the following link in a browser window to set a new password:"
			+ " http://localhost:8080/verification-service/password-reset.html?token=$tokenValue"
			+ " Thank you!";
	
	public void verifyEmail(UserDto userDto) {
		// Make sure region fo which FROM email address has been verified
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.build();
		
		String htmlBodyWithToken = HTML_BODY.replace("$tokenValue", userDto.getEmailVerificationToken());
		String textBodyWithToken = TEXT_BODY.replace("$tokenValue", userDto.getEmailVerificationToken());
		
		SendEmailRequest request = new SendEmailRequest()
			.withDestination(new Destination().withToAddresses(userDto.getEmail()))
			.withMessage(new Message()
				.withBody(new Body()
					.withHtml(new Content()
						.withCharset("UTF-8")
						.withData(htmlBodyWithToken))
					.withText(new Content()
						.withCharset("UTF-8")
						.withData(textBodyWithToken)))
				.withSubject(new Content()
					.withCharset("UTF-8")
					.withData(SUBJECT)))
			.withSource(FROM);
		
		client.sendEmail(request);
	}
	
	public boolean sendPasswordResetRequest(String firstName, String email, String token) {
		boolean retVal = false;
		
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
				.withRegion(Regions.US_EAST_1).build();
		
		String htmlBodyWithToken = PASSWORD_RESET_HTML_BODY.replace("$tokenValue", token)
				.replace("$firstName", firstName);
		
		String textBodyWithToken = PASSWORD_RESET_TEXT_BODY.replace("$tokenValue", token)
				.replace("$firstName", firstName);
		
		SendEmailRequest request = new SendEmailRequest()
			.withDestination(new Destination().withToAddresses(email))
			.withMessage(new Message()
				.withBody(new Body()
					.withHtml(new Content()
						.withCharset("UTF-8")
						.withData(htmlBodyWithToken))
					.withText(new Content()
						.withCharset("UTF-8")
						.withData(textBodyWithToken)))
				.withSubject(new Content()
					.withCharset("UTF-8")
					.withData(PASSWORD_RESET_SUBJECT)))
			.withSource(FROM);
		
		SendEmailResult result = client.sendEmail(request);
		
		// Check if email sent correctly
		if (result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty())) {
			retVal = true;
		}
		
		return retVal;
	}
}
