package com.web.forumTunisia.notification;


import com.web.forumTunisia.comment.Comment;

import com.web.forumTunisia.comment.CommentService;
import com.web.forumTunisia.poste.Poste;

import com.web.forumTunisia.poste.PosteService;
import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {
    private final NotificationRepository notifRepository;
    private final UserRepository userRepository;
   private final PosteService posteService;
   private final CommentService commentService ;

    public Notification saveNotification(Notification notif)
    {  notif.setDateCreate(new Date());
      return   notifRepository.save(notif);
    }
    public long countUnreadNotifications(String username) {
        Optional<User> user =userRepository.findByUsername(username);
        return notifRepository.countByRecipientsContainingAndReadFalse(user.get());
    }

    public List<Notification> getNotificationsForUser(String username) {
        // Vérification de l'existence de l'utilisateur
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (!optionalUser.isPresent()) {
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        try {
            List<Notification> notifications = notifRepository.findByRecipientsUsername(username, Sort.by(Sort.Direction.DESC, "dateCreate"));
            List<Notification> filteredNotifications = new ArrayList<>();

            for (Notification notification : notifications) {
                if (notification.isEnabled()) { // Vérifie si la notification est activée
                    filteredNotifications.add(notification); // Ajoute à la liste filtrée
                }
            }

            return filteredNotifications;} catch (Exception e) {

            throw new RuntimeException("Erreur lors de la récupération des notifications", e);
        }
    }
    public List<Notification> getNotificationsReadFalseForUser(String username) {
        return notifRepository.findByRecipientsUsernameAndReadFalse(username);
    }






    public void markNotificationsAsRead(String username) {
        List<Notification> notifications = notifRepository.findByRecipientsUsernameAndReadFalse(username);

        for (Notification notification : notifications) {

            notification.setRead(true);
        }
        notifRepository.saveAll(notifications);
    }


    public void deleteNotification(Long id) {
        Optional<Notification> notificationOpt = notifRepository.findById(id);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.getRecipients().clear(); // Clear recipients list before deletion
            notifRepository.delete(notification);
        }
    }

    public void disableNotification(Long notificationId) {
        Optional<Notification> notificationOpt = notifRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setEnabled(false); // Mise à jour du champ enabled à false
            notifRepository.save(notification); // Sauvegarder la notification mise à jour
        } else {
            throw new EntityNotFoundException("Notification non trouvée avec l'ID : " + notificationId);
        }
    }
}
