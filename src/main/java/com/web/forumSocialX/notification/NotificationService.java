package com.web.forumSocialX.notification;


import com.web.forumSocialX.comment.CommentService;

import com.web.forumSocialX.poste.PosteService;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

    public Notification updateNotification(long id, Notification updatedNotif) {
        // Récupérer la notification existante par ID
        Optional<Notification> optionalNotif = notifRepository.findById(id);

        if (optionalNotif.isPresent()) {
            Notification existingNotif = optionalNotif.get();

            // Mettre à jour les champs nécessaires de la notification existante
            existingNotif.setMessage(updatedNotif.getMessage());
            existingNotif.setActor(updatedNotif.getActor());
            existingNotif.setRecipients(updatedNotif.getRecipients());
            existingNotif.setPoste(updatedNotif.getPoste());
            existingNotif.setComment(updatedNotif.getComment());
            existingNotif.setReaction(updatedNotif.getReaction());
            existingNotif.setDateCreate(updatedNotif.getDateCreate());
            existingNotif.setRead(updatedNotif.isRead());
            existingNotif.setInteraction (updatedNotif.getInteraction());
            existingNotif.setEnabled (updatedNotif.isEnabled());
            // Sauvegarder les modifications
            return notifRepository.save(existingNotif);
        } else {
            throw new EntityNotFoundException("Notification avec ID " + id + " non trouvée.");
        }
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

    public Optional<Notification >findById(Long id)

    {

        return  notifRepository.findById(id);


    }
}
