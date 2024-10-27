package com.web.forumTunisia.follow;


import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;




    public void followUser(Long followerId, Long followedId) {
        User follower = userRepository.findById(followerId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        User followed = userRepository.findById(followedId).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));


        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        follow.setFollowedAt(new Date());

        followRepository.save(follow);
    }

    public void unfollowUser(Long followerId, Long followedId) {
        // Trouver le suivi par followerId et followedId
        Follow follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .orElseThrow(() -> new RuntimeException("Suivi non trouvé"));

        // Supprimer le suivi
        followRepository.delete(follow);
    }

    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    public List<User> getFollowers(Long userId) {
        return followRepository.findFollowersByUserId(userId);
    }

    public List<User> getFollowedUsers(Long userId) {
        return followRepository.findFollowedByUserId(userId);
    }
}