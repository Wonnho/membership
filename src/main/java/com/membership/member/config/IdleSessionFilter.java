package com.membership.member.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

public class IdleSessionFilter extends OncePerRequestFilter {
    public static final String LAST_ACTIVITY = "coffee.lastUserActivity";
    public static final long TIMEOUT_MS = 30L * 60 * 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException, IOException {
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        HttpSession session=request.getSession(false);
        if(session!=null && auth!=null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            long now=System.currentTimeMillis();
            Object previous=session.getAttribute(LAST_ACTIVITY);
            if(previous instanceof Long && now-(Long)previous>=TIMEOUT_MS) {
                new SecurityContextLogoutHandler().logout(request,response,auth);
                response.sendRedirect(request.getContextPath()+"/login?timeout");
                return;
            }
            String path=request.getServletPath();
            if(!path.startsWith("/css/") && !path.startsWith("/js/") && !path.startsWith("/images/")
                    && !path.startsWith("/coffee-images/") && !path.equals("/favicon.ico"))
                session.setAttribute(LAST_ACTIVITY,now);
        }
        chain.doFilter(request,response);
    }
}
