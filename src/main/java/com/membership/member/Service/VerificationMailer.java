package com.membership.member.Service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationMailer {
    private final ObjectProvider<JavaMailSender> sender;
    private final String host;
    private final String from;
    public VerificationMailer(ObjectProvider<JavaMailSender> sender,
            @Value("${spring.mail.host:}") String host,
            @Value("${coffee.mail.from:}") String from) {
        this.sender=sender; this.host=host; this.from=from;
    }
    public void sendCode(String email,String code) {
        if (host.isBlank() || from.isBlank() || sender.getIfAvailable()==null)
            throw new IllegalStateException("Email delivery is not configured yet. Please contact the administrator.");
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom(from); message.setTo(email);
        message.setSubject("Mustache Coffee - verify your email");
        message.setText("Your email verification code is: "+code
                +"\n\nEnter this code on the email verification page. It expires in 10 minutes and works once."
                +"\nAfter verification, log in with the password you chose at signup."
                +"\n\nIf you did not request this, ignore this message.");
        try { sender.getObject().send(message); }
        catch (MailException ex) {
            throw new IllegalStateException("We could not send the verification email. Please try again later.");
        }
    }
}
