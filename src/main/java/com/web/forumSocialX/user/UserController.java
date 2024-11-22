package com.web.forumSocialX.user;

import com.web.forumSocialX.firebase.FirebaseService;
import com.web.forumSocialX.firebase.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping(path = "/user")
@RequiredArgsConstructor

public class UserController  {

    private final FirebaseStorageService firebaseStorageService;

    private final  UserService  userService;
    private final FirebaseService firebaseService;
    private final SimpMessagingTemplate messagingTemplate;
    @GetMapping("/findByUsername/{username}")
    public ResponseEntity<User> findByUsername(@PathVariable(value = "username") String username) {
        log.info("Searching for user with username: {}", username);

        return userService.findByUsername(username)

                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/findById/{id}")
    public ResponseEntity<User> findById(@PathVariable(value = "id") Long id) {
        log.info("Searching for user with Id: {}", id);

        return userService.findById(id)

                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/findAll")
    public List<User> findAll()
    {
        return userService.findAllRoles();
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User u = userService.updateUser(id, user);

            return ResponseEntity.ok(u);

    }


    @PutMapping("/uploadImage/{id}")
    public ResponseEntity<Object> updateUserImage(@RequestParam("file") MultipartFile multipartFile, @PathVariable Long id) {
        User u = userService.updateUserImage(id, multipartFile);
        if (u != null) {
            // Créer un objet Map pour construire la réponse JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "User updated successfully");

            // Renvoyer la réponse JSON
            return ResponseEntity.ok(response);
        }
        // Si l'utilisateur n'est pas trouvé ou si une autre erreur se produit, renvoyer une réponse appropriée
        return ResponseEntity.badRequest().body("User not found or an error occurred");
    }
  /*  @MessageMapping("/user.addUser")
    @SendTo("/user/public")
    public User addUser(
            @Payload User user
    ) {
        userService.connect(user);
        return user;
    }

    @MessageMapping("/user.disconnectUser")
    @SendTo("/user/public")
    public User disconnectUser(
            @Payload User user
    ) {
        userService.disconnect(user);
        return user;
    }*/

    @PutMapping("/connect")
    public ResponseEntity<Void> connectStatus(@RequestBody User user) {
        userService.connect(user);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/offline-duration/{username}")
    public ResponseEntity<String> getOfflineDuration(@PathVariable String username) {
        Duration duration = userService.getOfflineDuration(username);
        if (duration.isZero()) {
            System.out.printf("L'utilisateur est connecté");
            return ResponseEntity.ok(null);
        } else {
            String formattedDuration= userService.formatDuration(duration);
            System.out.printf("Durée de déconnexion pour l'utilisateur " + formattedDuration);
            return ResponseEntity.ok(formattedDuration );
        }
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        messagingTemplate.convertAndSend("/topic/user-blocked/" + userId, "User is blocked");
        userService.deleteUserById(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bloquer un utilisateur par ID.
     *
     * @param userId ID de l'utilisateur à bloquer.
     * @return Réponse HTTP.
     */
    @PatchMapping("/block/{userId}")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        messagingTemplate.convertAndSend("/topic/user-blocked/" + userId, "User is blocked");

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/unblock/{userId}")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.noContent().build();}
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal connectedUser) {
        try {
            userService.changePassword(request, connectedUser);
            return ResponseEntity.ok("Password updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
