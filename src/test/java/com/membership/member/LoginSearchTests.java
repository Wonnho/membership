package com.membership.member;

import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "coffee.email-verification.enabled=true")
@AutoConfigureMockMvc
@Transactional
class LoginSearchTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired PasswordEncoder passwords;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.membership.member.Service.VerificationMailer mailer;

    @Test
    void navigationAndLoginPageRender() throws Exception {
        mvc.perform(get("/coffee"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("name=\"q\"")));
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/members").accept("text/html")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void renderedFormTokenIsAccepted() throws Exception {
        var page = mvc.perform(get("/membership/signup")).andExpect(status().isOk()).andReturn();
        var token = java.util.regex.Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"")
                .matcher(page.getResponse().getContentAsString());
        assertThat(token.find()).isTrue();
        mvc.perform(post("/join").session((MockHttpSession) page.getRequest().getSession(false))
                        .param("_csrf", token.group(1)).param("email", "form@example.com")
                        .param("password", "test-password"))
                .andExpect(redirectedUrl("/membership/verify"));
    }

    @Test
    void signupLoginAndLogoutWorkWithSession() throws Exception {
        mvc.perform(post("/join").with(csrf()).param("email", " User@Example.com ")
                        .param("password", "test-password"))
                .andExpect(redirectedUrl("/membership/verify"));
        Member member = members.findByEmail("user@example.com").orElseThrow();
        assertThat(member.getPassword()).isNotEqualTo("test-password");
        assertThat(passwords.matches("test-password", member.getPassword())).isTrue();
        mvc.perform(post("/login").with(csrf()).param("email",member.getEmail()).param("password","test-password"))
                .andExpect(unauthenticated());
        var sentCode=org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(mailer).sendCode(org.mockito.ArgumentMatchers.eq("user@example.com"),sentCode.capture());
        mvc.perform(post("/membership/verify").with(csrf()).param("email",member.getEmail()).param("code",sentCode.getValue()))
                .andExpect(redirectedUrl("/login?verified"));

        mvc.perform(post("/login").with(csrf()).param("email", "user@example.com")
                        .param("password", "wrong"))
                .andExpect(redirectedUrl("/login?error")).andExpect(unauthenticated());
        var result = mvc.perform(post("/login").with(csrf()).param("email", " USER@example.com ")
                        .param("password", "test-password"))
                .andExpect(redirectedUrl("/coffee")).andExpect(authenticated()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(get("/coffee").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Logout")))
                .andExpect(content().string(not(containsString("href=\"/login\""))));
        mvc.perform(get("/members/" + member.getId()).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(member.getPassword()))));
        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void rejectsDuplicateAndInvalidSignupAndMissingCsrf() throws Exception {
        members.save(new Member("exists@example.com", passwords.encode("test-password")));
        mvc.perform(post("/join").with(csrf()).param("email", "EXISTS@example.com")
                        .param("password", "test-password"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already registered")));
        mvc.perform(post("/join").with(csrf()).param("email", "new@example.com")
                        .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("at least 8 characters")));
        mvc.perform(post("/join").param("email", "new@example.com").param("password", "test-password"))
                .andExpect(status().isForbidden());
        assertThat(members.findByEmail("new@example.com")).isEmpty();
    }

    @Test
    void membersCannotReadModifyOrDeleteOtherAccounts() throws Exception {
        Member other = members.save(new Member("other@example.com", passwords.encode("test-password")));
        mvc.perform(get("/members/" + other.getId()).with(user("someone@example.com")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/members/update/" + other.getId()).with(user("someone@example.com"))
                        .with(csrf()).param("email", "changed@example.com").param("password", "new-password"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/members/delete/" + other.getId()).with(user("someone@example.com")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void accountUpdateHashesNewPasswordAndRequiresLoginAgain() throws Exception {
        Member member = members.save(new Member("edit@example.com", passwords.encode("old-password")));
        mvc.perform(post("/members/update/" + member.getId()).with(user("edit@example.com")).with(csrf())
                        .param("email", "edited@example.com").param("password", "new-password"))
                .andExpect(redirectedUrl("/membership/verify")).andExpect(unauthenticated());
        Member updated = members.findByEmail("edited@example.com").orElseThrow();
        assertThat(passwords.matches("new-password", updated.getPassword())).isTrue();
    }

    @Test
    void searchIsPartialCaseInsensitiveAndHandlesEmptyOrNoMatches() throws Exception {
        mvc.perform(get("/coffee/read").param("q", " LAT "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("resultCount", 1))
                .andExpect(content().string(containsString("Latte")))
                .andExpect(content().string(not(containsString("Americano"))));
        mvc.perform(get("/coffeeboard/read").param("q", "no-such-coffee"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("resultCount", 0))
                .andExpect(content().string(containsString("No coffees match your search")));
        mvc.perform(get("/coffee/read").param("q", " "))
                .andExpect(status().isOk())
                .andExpect(model().attribute("resultCount", 3));
        mvc.perform(get("/coffee/read").param("q", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void coffeeFormsStillRenderAndPublicApiStillWorks() throws Exception {
        mvc.perform(get("/coffeeboard/register").with(user("admin@coffemaker.com").roles("ADMIN"))).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/coffeeboard/modify/1").with(user("admin@coffemaker.com").roles("ADMIN"))).andExpect(status().isOk());
        mvc.perform(post("/api/coffee").with(user("admin@coffemaker.com").roles("ADMIN")).contentType("application/json")
                        .content("{\"coffee\":\"Test brew\",\"price\":1200}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/coffeeboard/modify/1").with(user("admin@coffemaker.com").roles("ADMIN")).with(csrf()).param("coffee", "Americano")
                        .param("price", "4600").param("image", "/images/coffee/americano.jpg"))
                .andExpect(redirectedUrl("/coffee/read"));
    }
}
