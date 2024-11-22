package com.web.forumSocialX.auth;


import com.web.forumSocialX.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {


    private final AuthenticationService service;
    private final UserService userService;
    @PostMapping("/signup")
    public ResponseEntity<AuthenticationResponse> signUp(
            @Valid @RequestBody RegisterRequest request
    ) {
        try {
            AuthenticationResponse response = service.register(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error and return an appropriate HTTP status
            log.error("Error during signup: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthenticationResponse.builder().messageError(e.getMessage()).build());
        }
    }
    @GetMapping("/verify")
    public ResponseEntity<Void> verify(@RequestParam("token") String token, HttpServletResponse response) {
        return service.verify(token, response);
    }


 /*   @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signIn(
            @RequestBody AuthenticationRequest request
    ) {
        try {
            AuthenticationResponse response = service.authenticate(request);
            return ResponseEntity.ok(response);
        }
        catch (IllegalStateException e) {
            // Retourner une erreur spécifique si l'e-mail n'est pas vérifié
            log.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    AuthenticationResponse.builder()
                            .messageError(e.getMessage())
                            .build()
            );}
        catch (Exception e) {
            // Log the error and return an appropriate HTTP status
            log.error("Error during signin: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthenticationResponse.builder().messageError(e.getMessage()).build());
        }
    }*/
 @PostMapping("/signin")
 public ResponseEntity<AuthenticationResponse> signIn(@RequestBody AuthenticationRequest request) {
     try {
         // Tenter de s'authentifier
         AuthenticationResponse response = service.authenticate(request);

         // Si tout va bien, renvoyer la réponse avec les tokens
         return ResponseEntity.ok(response);

     } catch (EmailNotVerifiedException e) {
         log.error("Erreur email : {}", e.getMessage());
         // Assurer qu'on renvoie bien le message d'erreur complet
         return ResponseEntity.badRequest().body(
                 AuthenticationResponse.builder()
                         .messageError("Votre adresse e-mail n'a pas été vérifiée. Veuillez confirmer votre compte.")
                         .build()
         );

     } catch (AccountNotEnabledException e) {
         log.error("Erreur compte non activé : {}", e.getMessage());
         return ResponseEntity.badRequest().body(
                 AuthenticationResponse.builder()
                         .messageError("Votre compte n'est pas activé. Veuillez contacter l'administrateur.")
                         .build()
         );

     } catch (IllegalArgumentException e) {
         log.error("Erreur d'authentification (données invalides) : {}", e.getMessage());
         return ResponseEntity.badRequest().body(
                 AuthenticationResponse.builder()
                         .messageError("Nom d'utilisateur ou mot de passe incorrect")
                         .build()
         );

     } catch (Exception e) {
         log.error("Erreur interne lors de la connexion : {}", e.getMessage());
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                 AuthenticationResponse.builder()
                         .messageError("Nom d'utilisateur ou mot de passe incorrect")
                         .build()
         );
     }

 }
    @PostMapping("/signinAdmin")
    public ResponseEntity<AuthenticationResponse> signInAdmin(
            @RequestBody AuthenticationRequest request
    ) {
        try {
            AuthenticationResponse response = service.authenticateAdmin(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error and return an appropriate HTTP status
            log.error("Error during signin: {}", e.getMessage());
            return ResponseEntity.badRequest().body(AuthenticationResponse.builder().messageError(e.getMessage()).build());
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        try {
            AuthenticationResponse response = service.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Log the error and return an appropriate HTTP status
            log.error("Error during token refresh: {}", e.getMessage());
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED)
                    .body(AuthenticationResponse.builder().messageError(e.getMessage()).build());
        } catch (Exception e) {
            // Handle other exceptions
            log.error("Unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> Logout(@RequestBody LogoutRequest request)
    {
        userService.logout(request.getUsername());
        return ResponseEntity.ok("user logout");
    }
    @PostMapping("/heartbeat")
    public ResponseEntity<?> heartbeat(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        userService.updateLastActive(username);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        userService.updateStatusToDisconnected(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>>forgotPassword(@RequestBody PasswordResetRequest request) {
        try {
            String token = service.sendPasswordResetLink(request.getEmail());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Un lien de réinitialisation du mot de passe a été envoyé.");
            response.put("resetToken", token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {

            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Endpoint pour réinitialiser le mot de passe avec le token
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequest request) {
        try {
            service.resetPassword(request.getToken(), request.getPassword());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Mot de passe réinitialisé avec succès.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

