package com.web.forumSocialX.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)


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
        http.csrf(AbstractHttpConfigurer::disable) // Désactiver CSRF pour les API stateless
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

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

    /**
     * Configuration source pour CORS.
     */
    @Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Autorise toutes les origines (ajoutez votre frontend spécifique si nécessaire)
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200", "https://forum-socialx.vercel.app"));  // Définir ici vos origines spécifiques si nécessaire

        // Autorise tous les en-têtes
        config.setAllowedHeaders(Arrays.asList("*"));

        // Autorise toutes les méthodes HTTP
        config.setAllowedMethods(Arrays.asList("*"));

        // Applique la configuration CORS pour toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);  // Retourne un CorsFilter avec la configuration définie
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Autorise toutes les origines (ajoutez votre frontend spécifique si nécessaire)
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200", "https://forum-socialx.vercel.app"));  // Définir ici vos origines spécifiques si nécessaire

        // Autorise tous les en-têtes
        config.setAllowedHeaders(Arrays.asList("*"));

        // Autorise toutes les méthodes HTTP
        config.setAllowedMethods(Arrays.asList("*"));

        // Applique la configuration CORS pour toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);  // Retourne un CorsFilter avec la configuration définie
    }




}
