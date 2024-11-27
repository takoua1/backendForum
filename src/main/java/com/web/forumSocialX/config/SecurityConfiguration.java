package com.web.forumSocialX.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity


@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration   {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        return new HttpStatusReturningLogoutSuccessHandler();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
    {
        http.csrf(csrf ->csrf.disable())

                .cors(cros-> cros.disable())

                .authorizeHttpRequests(auth-> auth.requestMatchers("/auth/**","/user/**","/poste/**","/comment/**","/interaction/**","/chat-socket/**","/chat/**","/topic/**","/app/**",
                                "/groupe/**","/privie/**","/statistics/**","/ws-signale/**","/ws-mail/**","/mail/**","/follow/**","/blocks/**")
                .permitAll()
                        .requestMatchers("/signale/**").hasRole("ADMIN") // Exiger le rôle ADMIN pour certaines routes

                .anyRequest()
                .authenticated())
                .sessionManagement(session-> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                );
        return http.build();
    }
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        // Configure Spring Security to accept roles without 'ROLE_' prefix
        return new GrantedAuthorityDefaults(""); // Remove 'ROLE_' prefix
    }


}
