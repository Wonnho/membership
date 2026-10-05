package com.membership.member.config;

import com.membership.member.repository.MemberRepository;
import java.util.Locale;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean org.springframework.security.core.session.SessionRegistry sessionRegistry() {
        return new org.springframework.security.core.session.SessionRegistryImpl();
    }
    @Bean org.springframework.security.web.session.HttpSessionEventPublisher sessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(MemberRepository members,
            com.membership.member.Service.EmailVerificationService verification) {
        return email -> members.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .map(member -> User.withUsername(member.getEmail())
                        .password(member.getPassword()).roles(member.getRole())
                        .disabled(verification.isEnabled() && !member.isEmailVerified()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
    }

    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,
            org.springframework.security.core.session.SessionRegistry sessions) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/members", "/members/*/reset", "/h2-console/**").hasRole("ADMIN")
                .requestMatchers("/coffeeboard/register", "/coffeeboard/modify/**",
                        "/coffeeboard/remove/**", "/coffeeboard/upload").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/coffee", "/api/coffee/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/coffee/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/coffee/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/coffee/**").hasRole("ADMIN")
                .requestMatchers("/account", "/account/**", "/session/activity", "/members/**", "/basket", "/basket/**", "/orders/**").authenticated()
                .anyRequest().permitAll())
            .formLogin(form -> form.loginPage("/login").usernameParameter("email")
                    .defaultSuccessUrl("/coffee", true).failureUrl("/login?error").permitAll())
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(session -> session.maximumSessions(-1).sessionRegistry(sessions).expiredUrl("/login?closed"))
            .addFilterBefore(new IdleSessionFilter(), org.springframework.security.web.access.intercept.AuthorizationFilter.class)
            .logout(logout -> logout
                    .logoutSuccessHandler((request, response, auth) ->
                            response.sendRedirect(request.getContextPath() + "/login?logout"))
                    .deleteCookies("JSESSIONID"))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/coffee", "/api/coffee/**", "/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
