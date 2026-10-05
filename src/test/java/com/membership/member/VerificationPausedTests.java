package com.membership.member;

import com.membership.member.Service.VerificationMailer;
import com.membership.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class VerificationPausedTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @MockitoBean VerificationMailer mailer;

    @Test void signupAndLoginWorkWithoutEmailDelivery() throws Exception {
        String email="paused-test@example.com";
        try {
            mvc.perform(post("/join").with(csrf()).param("email",email).param("password","test-password"))
                .andExpect(redirectedUrl("/login?registered"));
            mvc.perform(post("/login").with(csrf()).param("email",email).param("password","test-password"))
                .andExpect(authenticated());
            mvc.perform(post("/membership/verify/resend").with(csrf()).param("email",email))
                .andExpect(redirectedUrl("/login"));
            verifyNoInteractions(mailer);
        } finally { members.findByEmail(email).ifPresent(members::delete); }
    }
}
