package com.web.forumSocialX.notification;



import com.web.forumSocialX.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/notification")
@RequiredArgsConstructor
public class NotificationController {

private final NotificationService notifService;
    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/specific")
    public void sendNotification(@Payload Notification notif) throws Exception {
        Notification not = notifService.saveNotification(notif);
        List<User> receivers = notif.getRecipients();
        String actor = not.getActor().getNom() + " " + not.getActor().getPrenom();

        for (User receiver : receivers) {
            NotificationWrapper notificationWrapper = new NotificationWrapper(
                    not.getId().toString(),
                    actor,
                    not.getActor().getImage(),
                    not.getMessage(),
                    not.getReaction(),
                    not.getPoste() !=null ? not.getPoste().getId().toString() : "",
                    not.getComment() != null ? not.getComment().getId().toString() : "",
                    not.getInteraction() != null ? not.getInteraction().getId().toString() : "",
                    receiver.getUsername(),
                    not.getDateCreate().toString()
            );

            messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/specific", notificationWrapper);
        }
    }

    @GetMapping("/unread-count/{username}")
    public long getUnreadCount(@PathVariable String username) {

        return notifService.countUnreadNotifications(username);
    }
    @GetMapping("/user/{username}")
    public List<Notification> getNotificationsForUser(@PathVariable String username) {

             List<Notification> notifications = notifService.getNotificationsForUser(username);
            return notifications;

    }
    @GetMapping("/user/notRead/{username}")
    public List<Notification> getNotificationsReadFalseForUser(@PathVariable String username) {
        return notifService.getNotificationsReadFalseForUser(username);
    }

    @PutMapping("/markAsRead/{username}")
    public ResponseEntity<Void> markNotificationsAsRead(@PathVariable String username) {
        notifService.markNotificationsAsRead(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notifService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<Void> disableNotification(@PathVariable Long id) {
        notifService.disableNotification(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Notification> updateNotification(
            @PathVariable Long id,
            @RequestBody Notification  updatedNotification) {
        try {
            Notification updatedNotif = notifService.updateNotification(id, updatedNotification);
            return ResponseEntity.ok(updatedNotif); // Retourne la notification mise à jour
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Retourne un 404 si la notification n'est pas trouvée
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Retourne un 500 pour d'autres erreurs
        }
    }
    @GetMapping("/findById/{id}")
    public ResponseEntity<Notification> findById(@PathVariable(value = "id") Long id) {
        log.info("Searching for user with Id: {}", id);

        return notifService.findById(id)

                .map(not-> ResponseEntity.ok().body(not))
                .orElse(ResponseEntity.notFound().build());
    }   }





