package com.membership.member;

import com.membership.member.Service.*;
import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import java.time.Instant;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(properties = "coffee.email-verification.enabled=true")
@AutoConfigureMockMvc
class EmailVerificationTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder passwords;
    @Autowired EmailVerificationService verification;
    @Autowired PasswordResetService resets;
    @MockitoBean VerificationMailer mailer;
    private final String email="verify-test@example.com";

    @AfterEach void cleanup() { members.findByEmail(email).ifPresent(members::delete); }
    private String signup() {
        verification.register(email,"test-password");
        ArgumentCaptor<String> code=ArgumentCaptor.forClass(String.class);
        verify(mailer).sendCode(eq(email),code.capture());
        assertThat(code.getValue()).matches("[0-9]{6}");
        return code.getValue();
    }

    @Test void signupOpensHaloVerificationAndLoginHasNoResendLink() throws Exception {
        var result=mvc.perform(post("/join").with(csrf()).param("email",email).param("password","test-password"))
            .andExpect(redirectedUrl("/membership/verify")).andReturn();
        mvc.perform(get("/membership/verify").session((org.springframework.mock.web.MockHttpSession)result.getRequest().getSession()))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Halo!")))
            .andExpect(content().string(containsString("value=\""+email+"\"")))
            .andExpect(content().string(not(containsString("alert alert-danger"))));
        mvc.perform(get("/login")).andExpect(content().string(not(containsString("Verify email / resend code"))));
        mvc.perform(post("/login").with(csrf()).param("email","admin@coffemaker.com").param("password","coffeemakerAdmin"))
            .andExpect(authenticated());
    }

    @Test void codeMustBeVerifiedBeforeLoginAndCannotBeReused() throws Exception {
        String code=signup();
        Member member=members.findByEmail(email).orElseThrow();
        assertThat(member.isEmailVerified()).isFalse();
        assertThat(member.getVerificationHash()).isNotEqualTo(code);
        assertThat(passwords.matches(code,member.getVerificationHash())).isTrue();
        mvc.perform(post("/login").with(csrf()).param("email",email).param("password","test-password"))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        mvc.perform(get("/account").with(httpBasic(email,"test-password"))).andExpect(status().isUnauthorized());
        mvc.perform(get("/membership/verify")).andExpect(status().isOk())
            .andExpect(content().string(not(containsString(code))));
        mvc.perform(post("/membership/verify").with(csrf()).param("email",email).param("code",code))
            .andExpect(redirectedUrl("/login?verified"));
        assertThat(members.findByEmail(email).orElseThrow().getVerificationHash()).isNull();
        mvc.perform(post("/login").with(csrf()).param("email",email).param("password","test-password"))
            .andExpect(authenticated());
        assertThat(verification.verify(email,code)).isFalse();
    }

    @Test void fiveWrongAttemptsAreCommittedAndBlockEvenCorrectCode() {
        String code=signup();
        String wrong=code.equals("000000") ? "111111" : "000000";
        for(int i=0;i<5;i++) assertThat(verification.verify(email,wrong)).isFalse();
        assertThat(members.findByEmail(email).orElseThrow().getVerificationAttempts()).isEqualTo(5);
        assertThat(verification.verify(email,code)).isFalse();
    }

    @Test void expirationAndResendReplaceOldCodeWithCooldown() {
        String old=signup();
        verification.resend(email);
        verify(mailer,times(1)).sendCode(eq(email),anyString());
        Member member=members.findByEmail(email).orElseThrow();
        member.issueVerification(member.getVerificationHash(),Instant.now().minusSeconds(601));
        members.save(member);
        assertThat(verification.verify(email,old)).isFalse();
        clearInvocations(mailer);
        verification.resend(email);
        ArgumentCaptor<String> fresh=ArgumentCaptor.forClass(String.class);
        verify(mailer).sendCode(eq(email),fresh.capture());
        if(!fresh.getValue().equals(old)) assertThat(verification.verify(email,old)).isFalse();
        assertThat(verification.verify(email,fresh.getValue())).isTrue();
        clearInvocations(mailer);
        verification.resend(email);
        verifyNoInteractions(mailer);
    }

    @Test void resendHourlyLimitAndUnknownEmailDoNotSend() {
        signup();
        Member member=members.findByEmail(email).orElseThrow();
        for(int i=0;i<4;i++) member.issueVerification(member.getVerificationHash(),Instant.now().minusSeconds(61));
        members.save(member);
        clearInvocations(mailer);
        verification.resend(email);
        verification.resend("not-registered@example.com");
        verifyNoInteractions(mailer);
    }

    @Test void deliveryFailureRollsBackSignupAndShowsError() throws Exception {
        doThrow(new IllegalStateException("Email delivery is unavailable.")).when(mailer).sendCode(anyString(),anyString());
        mvc.perform(post("/join").with(csrf()).param("email",email).param("password","test-password"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("formError","Email delivery is unavailable."));
        assertThat(members.findByEmail(email)).isEmpty();
    }

    @Test void passwordResetDoesNotVerifyTheEmail() throws Exception {
        signup();
        Member member=members.findByEmail(email).orElseThrow();
        String token=resets.issue(member.getId());
        resets.reset(token,"replacement-password");
        assertThat(members.findByEmail(email).orElseThrow().isEmailVerified()).isFalse();
        mvc.perform(post("/login").with(csrf()).param("email",email).param("password","replacement-password"))
            .andExpect(unauthenticated());
    }

    @Test void changingEmailRequiresVerificationAndCsrfIsRequired() throws Exception {
        String code=signup();
        verification.verify(email,code);
        mvc.perform(post("/membership/verify/resend").param("email",email)).andExpect(status().isForbidden());
        Member member=members.findByEmail(email).orElseThrow();
        // Same-email password changes keep verified status.
        verification.updateAccount(member.getId(),email,passwords.encode("new-password"));
        assertThat(members.findByEmail(email).orElseThrow().isEmailVerified()).isTrue();
        String changed="verify-changed@example.com";
        try {
            verification.updateAccount(member.getId(),changed,passwords.encode("new-password"));
            assertThat(members.findByEmail(changed).orElseThrow().isEmailVerified()).isFalse();
            mvc.perform(post("/login").with(csrf()).param("email",changed).param("password","new-password"))
                .andExpect(unauthenticated());
        } finally { members.findByEmail(changed).ifPresent(members::delete); }
    }
}
