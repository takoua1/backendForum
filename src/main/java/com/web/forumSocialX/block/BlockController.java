package com.web.forumSocialX.block;


import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blocks")
@RequiredArgsConstructor
@Slf4j
public class BlockController {

    private final BlockService blockService;
     private final UserService userService;
    @PostMapping("/block")
    public ResponseEntity<Map<String, String>>blockUser(@RequestParam Long blockerId, @RequestParam Long blockedId) {
        User blocker = userService.findById(blockerId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));;
        User blocked = userService.findById(blockedId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));;
        blockService.blockUser(blocker, blocked);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur bloqué avec succès.");
        return ResponseEntity.ok(response);
    }

    // Débloquer un utilisateur
    @DeleteMapping("/unblock/{blockerId}/{blockedId}")
    public ResponseEntity<Map<String, String>> unblockUser(
            @PathVariable Long blockerId,
            @PathVariable Long blockedId) {

        User blocker = userService.findById(blockerId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        User blocked = userService.findById(blockedId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        blockService.unblockUser(blocker, blocked);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur débloqué avec succès.");
        return ResponseEntity.ok(response);
    }

    // Lister les utilisateurs bloqués par un utilisateur
    @GetMapping("/blocked/{blockerId}")
    public ResponseEntity<List<User>> getBlockedUsers(@PathVariable Long blockerId) {
        User blocker = userService.findById(blockerId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));;
        List<User> blockedUsers = blockService.getBlockedUsers(blocker);
        return ResponseEntity.ok(blockedUsers);
    }

    // Lister les utilisateurs qui ont bloqué un utilisateur
    @GetMapping("/blockers/{blockedId}")
    public ResponseEntity<List<User>> getBlockers(@PathVariable Long blockedId) {
        User blocked = userService.findById(blockedId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));;
        List<User> blockers = blockService.getBlockers(blocked);
        return ResponseEntity.ok(blockers);
    }
    @GetMapping("/isBlocked")
    public ResponseEntity<Boolean> isBlocked(@RequestParam Long blockerId, @RequestParam Long blockedId) {
        User blocker = new User();
        blocker.setId(blockerId);

        User blocked = new User();
        blocked.setId(blockedId);

        boolean isBlocked = blockService.isBlocked(blocker, blocked);
        return ResponseEntity.ok(isBlocked);
    }
    @GetMapping("/isBlockedByUsername")
    public ResponseEntity<Boolean> isBlocked(@RequestParam String blockerUsername, @RequestParam String blockedUsername) {
        User blocker = userService.findByUsername(blockerUsername).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));;
        User blocked = userService.findByUsername(blockedUsername).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));;

        // Vérifier si les utilisateurs existent
        if (blocker == null || blocked == null) {
            return ResponseEntity.badRequest().body(false); // ou gérer l'erreur comme souhaité
        }

        boolean isBlocked = blockService.isBlocked(blocker, blocked);
        return ResponseEntity.ok(isBlocked);
    }
}
