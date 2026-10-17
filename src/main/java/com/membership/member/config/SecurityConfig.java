package com.membership.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {
    private static final String ADMIN_ROLE = "ADMIN";

    private static final String[] ADMIN_PAGES = {
            "/members",
            "/members/*/reset",
            "/h2-console/**",
            "/coffeeboard/register",
            "/coffeeboard/modify/**",
            "/coffeeboard/remove/**",
            "/coffeeboard/upload"
    };

    private static final String[] COFFEE_API = {
            "/api/coffee",
            "/api/coffee/**"
    };

    private static final String[] MEMBER_PAGES = {
            "/account",
            "/account/**",
            "/session/activity",
            "/members/**",
            "/basket",
            "/basket/**",
            "/orders/**"
    };

    private static final String[] CSRF_IGNORED_PATHS = {
            "/api/coffee",
            "/api/coffee/**",
            "/h2-console/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessions) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(ADMIN_PAGES).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, COFFEE_API).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PATCH, COFFEE_API).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT, COFFEE_API).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, COFFEE_API).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/coffee/read/*/reviews").authenticated()
                        .requestMatchers(MEMBER_PAGES).authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/coffee", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .maximumSessions(-1)
                        .sessionRegistry(sessions)
                        .expiredUrl("/login?closed"))
                .addFilterBefore(new IdleSessionFilter(), AuthorizationFilter.class)
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.sendRedirect(request.getContextPath() + "/login?logout"))
                        .deleteCookies("JSESSIONID"))
                .csrf(csrf -> csrf.ignoringRequestMatchers(CSRF_IGNORED_PATHS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
