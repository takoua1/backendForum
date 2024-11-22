package com.web.forumSocialX.interaction;

import com.web.forumSocialX.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InteractionService {
 public final InteractionRepository interactionRepository;
 public final UserRepository userRepository;
    public Interaction interaction(Interaction interaction) {

        interaction.setDatecreation(new Date());
        return interactionRepository.save(interaction);

    }

    public Optional<Interaction> findInteractionByUserIdAndPosteIdType(Long userId, Long posteId, String type) {
        List<Interaction> interactions = interactionRepository.findByUserIdAndPosteIdAndType(userId, posteId, type);
        if (interactions.isEmpty()) {
            return Optional.empty();
        } else if (interactions.size() > 1) {
            // Enregistrer un avertissement ou lancer une exception, selon vos exigences
            return Optional.of(interactions.get(0)); // Renvoyer le premier résultat
        } else {
            return Optional.of(interactions.get(0));
        }
    }
    public Optional<Interaction> findInteractionByUserIdAndCommentIdType(Long userId, Long commentId, String type) {
        List<Interaction> interactions = interactionRepository.findByUserIdAndCommentIdAndType(userId, commentId, type);
        if (interactions.isEmpty()) {
            return Optional.empty();
        } else if (interactions.size() > 1) {
            // Enregistrer un avertissement ou lancer une exception, selon vos exigences
            return Optional.of(interactions.get(0)); // Renvoyer le premier résultat
        } else {
            return Optional.of(interactions.get(0));
        }
    }

    public Interaction remove(Long id)
    {
        boolean exist = interactionRepository.existsById(id);
        Interaction inter = interactionRepository.getReferenceById(id);;
        if(exist)
        {
            interactionRepository.delete(inter);
            return inter;
        }
        return null;
    }

}
