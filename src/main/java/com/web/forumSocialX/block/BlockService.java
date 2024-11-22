package com.web.forumSocialX.block;


import com.web.forumSocialX.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BlockService {

    private final BlockRepository blockRepository;

    public void blockUser(User blocker, User blocked) {
        // Vérifier si l'utilisateur est déjà bloqué
        if (!isBlocked(blocker, blocked)) {
            Block block = new Block();
            block.setBlocker(blocker);
            block.setBlocked(blocked);
            block.setBlockedAt(new Date());
            blockRepository.save(block);
            System.out.println("Utilisateur bloqué avec succès.");
        } else {
            System.out.println("L'utilisateur est déjà bloqué.");
        }
    }

    // Débloquer un utilisateur
    @Transactional
    public void unblockUser(User blocker, User blocked) {
        if (isBlocked(blocker, blocked)) {
            blockRepository.deleteByBlockerAndBlocked(blocker, blocked);
            System.out.println("Utilisateur débloqué avec succès.");
        } else {
            System.out.println("Cet utilisateur n'est pas bloqué.");
        }
    }



    // Vérifier si un utilisateur est bloqué
    public boolean isBlocked(User blocker, User blocked) {
        return blockRepository.existsByBlockerAndBlocked(blocker, blocked);
    }

    // Lister les utilisateurs bloqués par un utilisateur
    public List<User> getBlockedUsers(User blocker) {
        List<Block> blocks = blockRepository.findByBlocker(blocker);
        return blocks.stream()
                .map(Block::getBlocked) // Extrait l'utilisateur bloqué
                .toList();
    }

    // Lister les utilisateurs qui ont bloqué un utilisateur
    public List<User> getBlockers(User blocked) {
        List<Block> blocks = blockRepository.findByBlocked(blocked);
        return blocks.stream()
                .map(Block::getBlocker) // Extrait celui qui a bloqué
                .toList();
    }
}
