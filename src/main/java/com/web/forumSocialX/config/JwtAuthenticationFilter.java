package com.web.forumSocialX.config;


import com.web.forumSocialX.user.UserService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String jwt = authHeader.substring(7);
            try {
                final String username = jwtService.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        // Token expiré
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expiré");
                        return;
                    }
                }
            } catch (RuntimeException e) {
                // Capture le cas où le token est expiré ou invalide
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expiré ou invalide");
                return;
            }
        }
        // Handle manual logout
        if ("/auth/logout".equals(request.getRequestURI()) && "POST".equals(request.getMethod())) {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String logoutJwt = authHeader.substring(7);
                String logoutUsername = jwtService.extractUsername(logoutJwt);
                if (logoutUsername != null) {
                    userService.logout(logoutUsername);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
