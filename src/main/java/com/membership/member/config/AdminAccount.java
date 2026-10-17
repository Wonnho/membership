package com.membership.member.config;

import com.membership.member.entity.Member;
import com.membership.member.repository.MemberRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AdminAccount {
    @Bean
    ApplicationRunner createAdmin(
            MemberRepository members,
            PasswordEncoder passwords,
            AdminProperties adminProperties) {
        return args -> {
            String email = adminProperties.email();
            if (members.findByEmail(email).isEmpty()) {
                Member admin = new Member(email, passwords.encode(adminProperties.password()));
                admin.makeAdmin();
                members.save(admin);
            }
        };
    }
}
