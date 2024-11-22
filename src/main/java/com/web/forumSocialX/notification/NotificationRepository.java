package com.web.forumSocialX.notification;

import com.web.forumSocialX.comment.Comment;
import com.web.forumSocialX.interaction.Interaction;
import com.web.forumSocialX.poste.Poste;
import com.web.forumSocialX.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientsUsernameAndReadFalse(String username);
    long countByRecipientsContainingAndReadFalse(User user);
    List<Notification> findByRecipientsUsername(String username, Sort dateCreate);
    List<Notification> findByEnabled(boolean enabled , Sort dateCreate);
    List<Notification> findByPoste(Poste poste);
    List<Notification> findByComment(Comment comment);
    List<Notification> findByInteraction(Interaction interaction);
}
