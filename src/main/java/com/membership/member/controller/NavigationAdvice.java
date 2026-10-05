package com.membership.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class NavigationAdvice {
    @ModelAttribute
    public void navigation(Model model, HttpServletRequest request, Authentication auth) {
        boolean loggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        String email = loggedIn ? auth.getName() : "";
        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("signedInEmail", email);
        model.addAttribute("username", email.contains("@") ? email.substring(0, email.indexOf('@')) : email);
        model.addAttribute("isAdmin", loggedIn && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        String query = request.getParameter("q");
        model.addAttribute("searchQuery", query == null ? "" : query.trim());
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        model.addAttribute("csrfToken", csrf == null ? "" : csrf.getToken());
    }
}
