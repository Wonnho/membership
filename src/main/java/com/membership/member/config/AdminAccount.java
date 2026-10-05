package com.membership.member.config;

import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccount {
    public static final String EMAIL = "admin@coffemaker.com";

    @Bean
    ApplicationRunner createAdmin(MemberRepository members, PasswordEncoder passwords,
            @Value("${coffee.admin-password:coffeemakerAdmin}") String password) {
        return args -> {
            if (members.findByEmail(EMAIL).isEmpty()) {
                Member admin = new Member(EMAIL, passwords.encode(password));
                admin.makeAdmin();
                members.save(admin);
            }
        };
    }
}
