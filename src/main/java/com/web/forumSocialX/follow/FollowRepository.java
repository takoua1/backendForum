package com.web.forumSocialX.follow;


import com.web.forumSocialX.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface FollowRepository extends JpaRepository<Follow, Long > {
    // Trouver les utilisateurs suivis par un utilisateur donné (follower)
    List<Follow> findByFollowerId(Long followerId);

    // Trouver les utilisateurs qui suivent un utilisateur donné (followed)
    List<Follow> findByFollowedId(Long followedId);
    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    @Query("SELECT f.follower FROM Follow f WHERE f.followed.id = :userId")
    List<User> findFollowersByUserId(@Param("userId") Long userId);

    @Query("SELECT f.followed FROM Follow f WHERE f.follower.id = :userId")
    List<User> findFollowedByUserId(@Param("userId") Long userId);

}
