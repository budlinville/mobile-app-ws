package com.walmart.app.ws.shared;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.walmart.app.ws.shared.dto.UserDto;

public class AmazonSES {
	// This address must be verified with Amazon SES.
	final String FROM = "budlinville@gmail.com";
	
	// The subject line for the email.
	final String SUBJECT = "One last step to complete your registration.";
	
	// The HTML body for the email.
	// TODO : Update localhost to production url when deploying in production
	final String HTML_BODY = "<h1>Please verify your email address</h1>"
			+ "<p>Thank you for registering with our mobile app. To complete registration process and be able to log in, "
			+ "please click on the following link: </p>"
			+ "<a href='http://localhost:8080/verification-service/email-verification.html?token=$tokenValue'>"
			+ "Final step to complete your registration" + "</a><br/><br/>"
			+ "Thanks you! We are waiting for you inside!";
	
	// The email body for recipients with non-HTML email clients
	final String TEXT_BODY = "Please verify your email address"
			+ "Thank you for registering with our mobile app. To complete registration process and be able to access your account, "
			+ "please open the following link in your browser window: "
			+ "http://localhost:8080/verification-service/email-verification.html?token=$tokenValue'>"
			+ "Thanks you! We are waiting for you inside!";
	
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
								.withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
								.withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
						.withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
				.withSource(FROM);
		client.sendEmail(request);
	}
}
