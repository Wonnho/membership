package com.membership.member;

import com.membership.member.config.IdleSessionFilter;
import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class MembershipSessionTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder passwords;
    @MockitoBean com.membership.member.Service.VerificationMailer mailer;
    @Value("${server.servlet.session.timeout}") String sessionTimeout;
    final String email="withdraw-test@example.com";
    @BeforeEach void create() {
        Member member=new Member(email,passwords.encode("test-password"));
        member.confirmEmail(); members.save(member);
    }
    @AfterEach void cleanup() { members.findByEmail(email).ifPresent(members::delete); }
    private MockHttpSession login() throws Exception {
        return (MockHttpSession)mvc.perform(post("/login").with(csrf()).param("email",email)
                .param("password","test-password")).andExpect(authenticated())
            .andReturn().getRequest().getSession(false);
    }
    @Test void withdrawalDeletesAccountBlocksLoginAndRevokesOtherSession() throws Exception {
        MockHttpSession first=login(), second=login();
        mvc.perform(post("/account/withdraw").session(first).with(csrf()).param("confirmClosure","true"))
            .andExpect(redirectedUrl("/login?closed")).andExpect(unauthenticated());
        assertThat(first.isInvalid()).isTrue();
        assertThat(members.findByEmail(email)).isEmpty();
        mvc.perform(post("/login").with(csrf()).param("email",email).param("password","test-password"))
            .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        mvc.perform(get("/account").session(second)).andExpect(redirectedUrl("/login?closed"));
        assertThat(second.isInvalid()).isTrue();
    }
    @Test void withdrawalRequiresCsrfAndConfirmationAndProtectsAdmin() throws Exception {
        MockHttpSession session=login();
        mvc.perform(post("/account/withdraw").session(session).param("confirmClosure","true"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/account/withdraw").session(session).with(csrf()).param("confirmClosure","false"))
            .andExpect(status().isBadRequest());
        assertThat(members.findByEmail(email)).isPresent();
        mvc.perform(post("/account/withdraw").with(user("admin@coffemaker.com").roles("ADMIN"))
                .with(csrf()).param("confirmClosure","true")).andExpect(status().isForbidden());
    }
    @Test void normalLogoutPreservesMembership() throws Exception {
        MockHttpSession session=login();
        mvc.perform(post("/logout").session(session).with(csrf())).andExpect(redirectedUrl("/login?logout"));
        assertThat(members.findByEmail(email)).isPresent();
        login();
    }
    @Test void expiredActivityCannotRefreshOrAccessProtectedPage() throws Exception {
        assertThat(sessionTimeout).isEqualTo("30m");
        MockHttpSession session=login();
        session.setAttribute(IdleSessionFilter.LAST_ACTIVITY,System.currentTimeMillis()-IdleSessionFilter.TIMEOUT_MS-1);
        mvc.perform(post("/session/activity").session(session).with(csrf()))
            .andExpect(redirectedUrl("/login?timeout")).andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();
        assertThat(members.findByEmail(email)).isPresent();
        MockHttpSession another=login();
        another.setAttribute(IdleSessionFilter.LAST_ACTIVITY,System.currentTimeMillis()-IdleSessionFilter.TIMEOUT_MS-1);
        mvc.perform(get("/account").session(another)).andExpect(redirectedUrl("/login?timeout"));
        assertThat(another.isInvalid()).isTrue();
    }
    @Test void realActivityRefreshesAnActiveSession() throws Exception {
        MockHttpSession session=login();
        long previous=System.currentTimeMillis()-29*60*1000;
        session.setAttribute(IdleSessionFilter.LAST_ACTIVITY,previous);
        mvc.perform(post("/session/activity").session(session).with(csrf())).andExpect(status().isNoContent());
        assertThat((Long)session.getAttribute(IdleSessionFilter.LAST_ACTIVITY)).isGreaterThan(previous);
        mvc.perform(get("/account").session(session)).andExpect(status().is3xxRedirection());
    }
}
