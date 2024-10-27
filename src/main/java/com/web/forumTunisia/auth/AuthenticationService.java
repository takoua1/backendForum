package com.web.forumTunisia.auth;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.forumTunisia.config.JwtService;

import com.web.forumTunisia.email.EmailService;
import com.web.forumTunisia.token.Token;
import com.web.forumTunisia.token.TokenRepository;
import com.web.forumTunisia.token.TokenService;
import com.web.forumTunisia.token.TokenType;
import com.web.forumTunisia.user.Status;
import com.web.forumTunisia.user.UserRepository;
import com.web.forumTunisia.user.User;

import com.web.forumTunisia.user.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository repository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .pays("")
                .tel(request.getTel())
                .status(request.getStatus())
                .role(request.getRole())
                .dateAuthenticated(new Date())

                .image(null)
                .lastActive(LocalDateTime.now())
                .enabled(false)
                .emailVerified(false)
                .build();

        log.info("Attempting to register user: {}", user);

        if (repository.findByUsername(user.getUsername()).isPresent()) {
            var messageError = "username already exists!";
            log.warn(messageError);
            return AuthenticationResponse.builder().messageError(messageError).build();
        } else if (repository.findByEmail(user.getEmail()).isPresent()) {
            var messageError = "Email already exists!";
            log.warn(messageError);
            return AuthenticationResponse.builder().messageError(messageError).build();
        } else {
            var savedUser = repository.save(user);
            log.info("User saved successfully: {}", savedUser);


            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getNom(), savedUser.getPrenom(), jwtToken);
            saveUserToken(savedUser, jwtToken);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        userService.updateLastActive(user.getUsername());

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .username(request.getUsername())
                .role(user.getRole().name())
                .build();
    }

    public AuthenticationResponse authenticateAdmin(AuthenticationRequest request) {
        // Vérifiez les informations d'identification de l'utilisateur
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Générez le token JWT ou autre authentification


        // Vérifiez le rôle
        if ("ADMIN".equals(user.getRole().name())) {
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);
            userService.updateLastActive(user.getUsername());
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .username(request.getUsername())
                    .role(user.getRole().name())
                    .build();
        } else {
            throw new RuntimeException("Accès refusé. Rôle insuffisant.");
        }
    }

    private void saveUserToken(User user, String jwtToken) {

        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid token format");
        }

        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if (username != null) {
            var user = this.repository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);

                return AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .build();
            } else {
                throw new IllegalArgumentException("Invalid or expired refresh token");
            }
        }

        throw new IllegalArgumentException("Invalid refresh token");
    }

    @Transactional
    public ResponseEntity<Void> verify(String token, HttpServletResponse response) {
        // Cherche le token dans la base de données
        Token verificationToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (verificationToken == null || verificationToken.isExpired() || verificationToken.isRevoked()) {
            try {
                // Redirige vers une page d'erreur
                response.sendRedirect("http://localhost:4200/error");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.badRequest().build();
        }

        try {
            // Récupérer l'utilisateur associé au token
            User user = verificationToken.getUser();

            // Activer l'utilisateur
            user.setEnabled(true);
            user.setEmailVerified(true);
            repository.save(user); // Sauvegarde des changements dans l'utilisateur

            // Marquez le token comme validé (optionnel)
            verificationToken.setValidatedAt(LocalDateTime.now());
            tokenRepository.save(verificationToken);

            // Redirection vers une page de succès après vérification
            response.sendRedirect("http://localhost:4200/success");
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            try {
                // Redirige en cas d'erreur interne
                response.sendRedirect("http://localhost:4200/error");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


        public String sendPasswordResetLink(String email) {
            User user = repository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec cet email : " + email));

            // Générer un token JWT
            var jwtToken = jwtService.generateToken(user);

            // Révoquer tous les anciens tokens et enregistrer le nouveau
            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);  // Le token expire dans 1h

            repository.save(user);

            // Envoyer l'email avec un lien générique (sans token visible)
            String resetUrl = "http://localhost:4200/reset-password";
            try {
                emailService.sendPasswordResetEmail(user.getEmail(),user.getNom(),user.getPrenom(), resetUrl);
            } catch (MessagingException e) {
                throw new RuntimeException("Erreur lors de l'envoi de l'e-mail", e);
            }

            // Retourner le token au frontend
            return jwtToken;
        }


        // Réinitialisation du mot de passe
    public void resetPassword(String token, String newPassword) {
        // Valider le token JWT
        if (!jwtService.validateToken(token)) {
            throw new IllegalArgumentException("Token de réinitialisation invalide ou expiré");
        }

        // Extraire le nom d'utilisateur à partir du token
        String username = jwtService.extractUsername(token);

        // Rechercher l'utilisateur par son nom d'utilisateur (ou email)
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé pour cet identifiant : " + username));

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));



        // Sauvegarder l'utilisateur avec le nouveau mot de passe
        repository.save(user);
    }

}
