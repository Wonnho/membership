package com.membership.member;

import com.membership.member.Service.VerificationMailer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class VerificationMailerTests {
    @Test void messageContainsCodeAndDestination() {
        var sender=mock(JavaMailSender.class);
        var factory=new DefaultListableBeanFactory();
        factory.registerSingleton("sender",sender);
        var mailer=new VerificationMailer(factory.getBeanProvider(JavaMailSender.class),"smtp.example.com","sender@example.com");
        mailer.sendCode("recipient@example.com","123456");
        var capture=org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(capture.capture());
        assertThat(capture.getValue().getTo()).containsExactly("recipient@example.com");
        assertThat(capture.getValue().getFrom()).isEqualTo("sender@example.com");
        assertThat(capture.getValue().getText()).contains("123456","10 minutes");
    }
    @Test void missingConfigurationDoesNotPretendToSend() {
        var factory=new DefaultListableBeanFactory();
        var mailer=new VerificationMailer(factory.getBeanProvider(JavaMailSender.class),"","");
        assertThatThrownBy(() -> mailer.sendCode("recipient@example.com","123456"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not configured");
    }
    @Test void smtpFailureHasSafeMessage() {
        var sender=mock(JavaMailSender.class);
        doThrow(new MailSendException("SMTP details")).when(sender).send(any(SimpleMailMessage.class));
        var factory=new DefaultListableBeanFactory();
        factory.registerSingleton("sender",sender);
        var mailer=new VerificationMailer(factory.getBeanProvider(JavaMailSender.class),"smtp.example.com","sender@example.com");
        assertThatThrownBy(() -> mailer.sendCode("recipient@example.com","123456"))
            .isInstanceOf(IllegalStateException.class).hasMessage("We could not send the verification email. Please try again later.");
    }
}
