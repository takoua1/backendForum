package com.web.forumTunisia.notification;



import com.web.forumTunisia.chat.ChatNotification;
import com.web.forumTunisia.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
                    not.getPoste().getId().toString(),
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
}
