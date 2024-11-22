package com.web.forumSocialX.block;

import com.web.forumSocialX.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

    List<Block> findByBlocker(User blocker); // Trouver tous les utilisateurs bloqués par un utilisateur

    List<Block> findByBlocked(User blocked); // Trouver tous les utilisateurs qui ont bloqué cet utilisateur

    boolean existsByBlockerAndBlocked(User blocker, User blocked); // Vérifie si un utilisateur a déjà bloqué un autre utilisateur

    @Modifying
    @Transactional
    @Query("DELETE FROM Block b WHERE b.blocker = :blocker AND b.blocked = :blocked")
    void deleteByBlockerAndBlocked(@Param("blocker") User blocker, @Param("blocked") User blocked);
}
