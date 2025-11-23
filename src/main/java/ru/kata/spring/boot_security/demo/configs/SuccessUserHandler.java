package ru.kata.spring.boot_security.demo.configs;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class SuccessUserHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        boolean isUser = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));

        if (isAdmin) {
            response.sendRedirect("/admin/users"); // Теперь это просто view
        } else if (isUser) {
            response.sendRedirect("/user"); // Просто view
        } else {
            response.sendRedirect("/");
        }
    }
}