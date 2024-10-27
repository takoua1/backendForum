package com.web.forumTunisia.follow;


import com.web.forumTunisia.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(path = "/follow")
@RequiredArgsConstructor
public class FollowController {
private final FollowService followService;


    @PostMapping("/{followerId}/{followedId}")
    public ResponseEntity<Map<String, String>> followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        try {
            followService.followUser(followerId, followedId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur suivi avec succès.");
            return ResponseEntity.ok(response);  // Renvoie un objet JSON
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    @DeleteMapping("/{followerId}/{followedId}")
    public  ResponseEntity<Map<String, String>> unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        try {
            followService.unfollowUser(followerId, followedId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur suivi avec succès.");
            return ResponseEntity.ok(response);  // Renvoie un objet JSON
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<User>> getFollowers(@PathVariable Long userId) {
        List<User> followers = followService.getFollowers(userId);
        return ResponseEntity.ok(followers);
    }
    @GetMapping("/followed/{userId}")
    public ResponseEntity<List<User>> getFollowedUsers(@PathVariable Long userId) {
        List<User> followedUsers = followService.getFollowedUsers(userId);
        return ResponseEntity.ok(followedUsers);
    }

    @GetMapping("/isFollowing/{followerId}/{followedId}")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long followerId, @PathVariable Long followedId) {
        boolean isFollowing = followService.isFollowing(followerId, followedId);
        return ResponseEntity.ok(isFollowing);
    }

}
