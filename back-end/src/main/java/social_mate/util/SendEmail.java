package social_mate.util;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SendEmail {
	
	private final JavaMailSender javaMailSender;
	
	
	public void sendOtpToEmail(String email, String otp) {
		
		
		SimpleMailMessage message=new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Your OTP Verification code");
		message.setText("Your OTP: "+ otp);
		
		javaMailSender.send(message);
		
	}

}
