package com.web.forumTunisia.message;

import com.web.forumTunisia.chat.Chat;
import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/message")
@RequiredArgsConstructor
public class MessageController {

private final MessageService messageService;
private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    @GetMapping("/receivers/{id}")
    public List<User> getReceivers(@PathVariable Long id) {
        return messageService.getReceiversByMessageId(id);
    }
    @GetMapping("/findById/{id}")
    public Optional<Message> geMessageById(@PathVariable Long id)
    {
        return messageService.geMessageById(id);
    }
    @GetMapping("/getMessages/{senderId}/{receiverId}")
    public List<Message> findMessagesBetweenUsers(@PathVariable Long senderId ,@PathVariable Long receiverId)
    {
        return messageService.findMessagesBetweenUsers(senderId,receiverId);
    }
    @GetMapping("/common-chats/{senderId1}/{senderId2}")
    public List<Chat> getCommonChats(@PathVariable  Long senderId1,@PathVariable  Long senderId2) {
        User sender1 = userService.findById(senderId1).orElseThrow(() -> new RuntimeException("User not found"));
        User sender2 =  userService.findById(senderId2).orElseThrow(() -> new RuntimeException("User not found"));
        return messageService.getCommonChats(sender1, sender2);
    }

    @PatchMapping("/markAsRead/{messageId}")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {

        try {

            messageService.markMessageAsRead(messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        messagingTemplate.convertAndSend("/topic/message-deleted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/last-message")
    public ResponseEntity<Message> getLastMessage() {
        Message lastMessage = messageService.findLastMessage();
        // Méthode pour trouver le dernier message
        return ResponseEntity.ok(lastMessage);
    }
}

