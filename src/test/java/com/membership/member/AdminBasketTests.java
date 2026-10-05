package com.membership.member;

import com.membership.member.config.AdminAccount;
import com.membership.member.Service.PasswordResetService;
import com.membership.member.entity.*;
import com.membership.member.repository.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminBasketTests {
    @Autowired MockMvc mvc;
    @Autowired MemberRepository members;
    @Autowired CoffeeRepository coffees;
    @Autowired CoffeeOrderRepository orders;
    @Autowired PasswordEncoder passwords;
    @Autowired PasswordResetService resets;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    com.membership.member.Service.VerificationMailer mailer;

    private Member member(String email) {
        Member member=new Member(email,passwords.encode("test-password"));
        member.confirmEmail();
        return members.save(member);
    }

    @Test void seededAdministratorCanLogIn() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("email",AdminAccount.EMAIL)
                .param("password","coffeemakerAdmin"))
            .andExpect(redirectedUrl("/coffee")).andExpect(authenticated().withRoles("ADMIN"));
        mvc.perform(get("/members").with(user(AdminAccount.EMAIL).roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Administrator access")));
        mvc.perform(post("/join").with(csrf()).param("email",AdminAccount.EMAIL)
                .param("password","test-password"))
            .andExpect(content().string(containsString("already registered")));
    }

    @Test void memberSeesOwnAccountButNoMemberListOrCoffeeEditActions() throws Exception {
        Member guest=member("guest@coffee.com");
        mvc.perform(get("/coffee").with(user(guest.getEmail()).roles("MEMBER")))
            .andExpect(content().string(not(containsString("Welcome Back"))))
            .andExpect(content().string(not(containsString("Browse Coffee List"))))
            .andExpect(content().string(containsString("href=\"/account\">guest")))
            .andExpect(content().string(containsString("/images/coffee-mark.svg")))
            .andExpect(content().string(containsString(">guest</a>")));
        mvc.perform(get("/members").with(user(guest.getEmail()).roles("MEMBER")))
            .andExpect(status().isForbidden());
        mvc.perform(get("/account").with(user(guest.getEmail())))
            .andExpect(redirectedUrl("/members/"+guest.getId()));
        mvc.perform(get("/members/"+guest.getId()).with(user(guest.getEmail())))
            .andExpect(status().isOk()).andExpect(content().string(not(containsString("Member List"))))
            .andExpect(content().string(containsString("Sign out")))
            .andExpect(content().string(containsString("Permanently quit membership")))
            .andExpect(content().string(not(containsString("/members/delete/"))));
        mvc.perform(get("/members/"+guest.getId()).with(user(AdminAccount.EMAIL).roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Member List")))
            .andExpect(content().string(containsString("Create password reset link")))
            .andExpect(content().string(not(containsString(guest.getPassword()))));
        mvc.perform(get("/coffee/read").with(user(guest.getEmail())))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Add to basket")))
            .andExpect(content().string(not(containsString("/coffeeboard/modify/"))));
        mvc.perform(get("/coffee/read").with(user(AdminAccount.EMAIL).roles("ADMIN")))
            .andExpect(content().string(containsString("/coffeeboard/modify/")));
    }

    @Test void directCoffeeWritesAndResetAreForbiddenToMembers() throws Exception {
        for(String path : new String[]{"/coffeeboard/register","/coffeeboard/modify/1"})
            mvc.perform(get(path).with(user("guest@coffee.com"))).andExpect(status().isForbidden());
        for(String path : new String[]{"/coffeeboard/register","/coffeeboard/modify/1",
                "/coffeeboard/remove/1","/coffeeboard/upload","/members/2/reset"})
            mvc.perform(post(path).with(user("guest@coffee.com")).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/coffee/1").with(user("guest@coffee.com"))
                .contentType("application/json").content("{\"price\":1}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/coffee/1").with(user("guest@coffee.com"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/coffee").with(user("guest@coffee.com"))
                .contentType("application/json").content("{\"coffee\":\"x\",\"price\":1}"))
            .andExpect(status().isForbidden());
    }

    @Test void welcomeAndLoggedOutFieldsAreCorrect() throws Exception {
        mvc.perform(post("/join").with(csrf()).param("email","new@coffee.com").param("password","test-password"))
            .andExpect(redirectedUrl("/login?registered"));
        mvc.perform(get("/login?registered").flashAttr("welcomeName","new"))
            .andExpect(content().string(containsString("Halo!")))
            .andExpect(content().string(not(containsString("Welcome Back"))));
        mvc.perform(get("/login?logout"))
            .andExpect(content().string(containsString("Bye, See you Again")))
            .andExpect(content().string(not(containsString("id=\"login-form\""))))
            .andExpect(content().string(not(containsString("coffeemakerAdmin"))))
            .andExpect(content().string(not(containsString("Welcome Back"))));
    }

    @Test void catalogPaginatesNineCardsAndKeepsSearch() throws Exception {
        for(int i=0;i<8;i++) {
            Coffee coffee=new Coffee(); coffee.setCoffee("Extra "+i); coffee.setPrice(1000);
            coffees.save(coffee);
        }
        mvc.perform(get("/coffee/read")).andExpect(model().attribute("coffees",hasSize(9)))
            .andExpect(model().attribute("hasNext",true));
        mvc.perform(get("/coffee/read").param("page","1")).andExpect(model().attribute("coffees",hasSize(2)))
            .andExpect(model().attribute("hasPrevious",true));
        mvc.perform(get("/coffee/read").param("q","extra")).andExpect(model().attribute("resultCount",8));
    }

    @Test void basketQuantitiesCheckoutAndOwnershipWork() throws Exception {
        Member buyer=member("buyer@coffee.com");
        Member other=member("otherbuyer@coffee.com");
        MockHttpSession session=new MockHttpSession();
        mvc.perform(post("/basket/add").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("id","1").param("quantity","2")).andExpect(redirectedUrl("/basket"));
        mvc.perform(post("/basket/add").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("id","2").param("quantity","1")).andExpect(redirectedUrl("/basket"));
        mvc.perform(get("/basket").session(session).with(user(buyer.getEmail())))
            .andExpect(status().isOk()).andExpect(model().attribute("total",14000L));
        mvc.perform(post("/basket/update").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("id","1").param("quantity","3")).andExpect(redirectedUrl("/basket"));
        String key=(String)session.getAttribute("checkoutKey");
        String receipt=mvc.perform(post("/basket/checkout").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("checkoutKey",key).param("total","1"))
            .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        CoffeeOrder order=orders.findByCheckoutKeyAndMemberId(key,buyer.getId()).orElseThrow();
        assertThat(order.getTotal()).isEqualTo(18500);
        assertThat(order.getItems()).hasSize(2);
        mvc.perform(get(receipt).with(user(buyer.getEmail()))).andExpect(status().isOk())
            .andExpect(content().string(containsString("18500")));
        mvc.perform(get(receipt).with(user(other.getEmail()))).andExpect(status().isForbidden());
        long count=orders.count();
        mvc.perform(post("/basket/checkout").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("checkoutKey",key)).andExpect(redirectedUrl(receipt));
        assertThat(orders.count()).isEqualTo(count);
        mvc.perform(get("/basket").session(session).with(user(buyer.getEmail())))
            .andExpect(model().attribute("empty",false))
            .andExpect(model().attribute("items",hasSize(2)))
            .andExpect(model().attribute("alreadyOrdered",true))
            .andExpect(content().string(not(containsString(">Place order</button>"))));
        mvc.perform(get("/coffee/read").session(session).with(user(buyer.getEmail()))).andExpect(status().isOk());
        mvc.perform(get("/basket").session(session).with(user(buyer.getEmail())))
            .andExpect(model().attribute("items",hasSize(2))).andExpect(model().attribute("total",18500L));
        mvc.perform(post("/basket/update").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("id","1").param("quantity","3")).andExpect(redirectedUrl("/basket"));
        assertThat(session.getAttribute("checkoutKey")).isEqualTo(key);
        mvc.perform(post("/basket/add").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("id","3").param("quantity","1")).andExpect(redirectedUrl("/basket"));
        String newKey=(String)session.getAttribute("checkoutKey");
        assertThat(newKey).isNotEqualTo(key);
        mvc.perform(get("/basket").session(session).with(user(buyer.getEmail())))
            .andExpect(model().attribute("alreadyOrdered",false)).andExpect(model().attribute("items",hasSize(3)));
        mvc.perform(post("/basket/checkout").session(session).with(user(buyer.getEmail())).with(csrf())
                .param("checkoutKey",newKey)).andExpect(status().is3xxRedirection());
        assertThat(orders.count()).isEqualTo(count+1);
        assertThat(orders.findByCheckoutKeyAndMemberId(newKey,buyer.getId()).orElseThrow().getTotal()).isEqualTo(24000L);
    }

    @Test void basketRejectsBadQuantitiesAndIsolatesAccounts() throws Exception {
        member("first@coffee.com"); member("second@coffee.com");
        MockHttpSession session=new MockHttpSession();
        mvc.perform(post("/basket/add").session(session).with(user("first@coffee.com")).with(csrf())
                .param("id","1").param("quantity","0")).andExpect(status().isBadRequest());
        mvc.perform(post("/basket/add").session(session).with(user("first@coffee.com")).with(csrf())
                .param("id","1").param("quantity","100")).andExpect(status().isBadRequest());
        mvc.perform(post("/basket/add").session(session).with(user("first@coffee.com")).with(csrf())
                .param("id","1").param("quantity","1")).andExpect(redirectedUrl("/basket"));
        mvc.perform(get("/basket").session(session).with(user("second@coffee.com")))
            .andExpect(model().attribute("empty",true));
        mvc.perform(get("/basket").accept("text/html")).andExpect(redirectedUrl("/login"));
    }

    @Test void resetLinkIsAdminOnlySingleUseAndNewPasswordWorks() throws Exception {
        Member target=member("reset@coffee.com");
        String path=(String)mvc.perform(post("/members/"+target.getId()+"/reset")
                .with(user(AdminAccount.EMAIL).roles("ADMIN")).with(csrf()))
            .andExpect(status().isOk()).andReturn().getModelAndView().getModel().get("resetPath");
        String token=path.substring(path.indexOf('=')+1);
        assertThat(target.getResetTokenHash()).isNotEqualTo(token);
        mvc.perform(get("/password/reset").param("token",token)).andExpect(status().isOk())
            .andExpect(model().attribute("validLink",true));
        mvc.perform(post("/password/reset").with(csrf()).param("token",token)
                .param("password","replacement-password").param("confirmation","replacement-password"))
            .andExpect(redirectedUrl("/login?updated"));
        assertThat(resets.valid(token)).isFalse();
        assertThat(passwords.matches("replacement-password",target.getPassword())).isTrue();
        mvc.perform(post("/login").with(csrf()).param("email",target.getEmail())
                .param("password","replacement-password")).andExpect(authenticated());
        mvc.perform(post("/password/reset").with(csrf()).param("token",token)
                .param("password","different-password").param("confirmation","different-password"))
            .andExpect(model().attribute("validLink",false));
    }

    @Test void oldAndExpiredResetLinksDoNotWork() {
        Member target=member("expired@coffee.com");
        String old=resets.issue(target.getId());
        String current=resets.issue(target.getId());
        assertThat(resets.valid(old)).isFalse();
        target.issueReset(target.getResetTokenHash(),Instant.now().minusSeconds(1));
        assertThat(resets.valid(current)).isFalse();
        assertThatThrownBy(() -> resets.reset(current,"new-password")).isInstanceOf(IllegalArgumentException.class);
    }
}
