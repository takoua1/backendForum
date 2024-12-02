package com.web.forumSocialX.chat;


import com.web.forumSocialX.firebase.FirebaseStorageService;
import com.web.forumSocialX.groupe.Groupe;
import com.web.forumSocialX.message.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RequestMapping(path = "/chat")

@RequiredArgsConstructor
public class ChatController {


    private final ChatService chatService;
    private final UserService userService;
    private final MessageService messageService;
    private final FirebaseStorageService firebaseStorageService;
    private final Map<Long, Set<String>> activeUsers = new ConcurrentHashMap<>();
    private Map<String, Long> userChatMap = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;

    public void processMessage(@Payload Message message) {

    }


    @MessageMapping("/send")
    public void sendMessage(@Payload MessageWrapper messageWrapper) throws Exception {

        // Log l'objet messageWrapper reçu
        System.out.printf("Received messageWrapper: %s%n", messageWrapper);

        if (messageWrapper == null) {
            logger.error("Received null messageWrapper");
            return;
        }

        Message message = messageWrapper.getMessage();
        if (message == null) {
            logger.error("Received null message within messageWrapper");
            return ;
        }

        String fileUrl = messageWrapper.getFileUrl();
        String fileType = messageWrapper.getFileType();

        if ("image".equals(fileType)) {
            message.setImage(fileUrl);
        } else if ("video".equals(fileType)) {
            message.setVideo(fileUrl);
        } else if ("audio".equals(fileType)) {
            message.setAudio(fileUrl);
        }

        Message msg = messageService.sendMessage(message);

        if (msg == null) {
            logger.error("sendMessage returned null");
            return ;
        }

        List<User> receivers = message.getReceivers();
        if (receivers == null || receivers.isEmpty()) {
            logger.error("No receivers specified for the message");
            return ;
        }

        for (User receiver : receivers) {
            // Log chaque utilisateur récepteur
            System.out.println("Sending message to: " + receiver.getUsername());

            messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages",
                    new ChatNotification(
                            msg.getId().toString(),
                            msg.getSender().getId().toString(),
                            msg.getSender().getUsername(),
                            msg.getSender().getNom(),
                            msg.getSender().getPrenom(),
                            msg.getSender().getEmail(),
                            msg.getSender().getImage(),
                            receiver.getUsername(),
                            msg.getImage(),
                            msg.getVideo(),
                            msg.getAudio(),
                            msg.getContent(),
                            msg.getTime().toString(),
                            msg.getRead()

                    ));
        }
      User sender = msg.getSender();
        messagingTemplate.convertAndSendToUser(sender.getUsername(), "/queue/messages",
                new ChatNotification(
                        msg.getId().toString(),
                        msg.getSender().getId().toString(),
                        msg.getSender().getUsername(),
                        msg.getSender().getNom(),
                        msg.getSender().getPrenom(),
                        msg.getSender().getEmail(),
                        msg.getSender().getImage(),
                        msg.getSender().getUsername(),
                        msg.getImage(),
                        msg.getVideo(),
                        msg.getAudio(),
                        msg.getContent(),
                        msg.getTime().toString(),
                        msg.getRead()

                ));

        System.out.println("Message sent: " + msg);

    }
    @MessageMapping("/sendGroup/{groupId}")
    public void sendGroupe(@DestinationVariable Long groupId, @Payload MessageWrapper messageWrapper) throws Exception {
        // Vérification si messageWrapper est null
        if (messageWrapper == null) {
            logger.error("Received null messageWrapper");
            return;
        }

        // Extraction du message et du groupe depuis messageWrapper
        Message message = messageWrapper.getMessage();
        Groupe groupe = messageWrapper.getGroupe();

        // Vérification si le message est null
        if (message == null) {
            logger.error("Received null message within messageWrapper");
            return;
        }

        // Récupération de l'URL et du type de fichier depuis messageWrapper
        String fileUrl = messageWrapper.getFileUrl();
        String fileType = messageWrapper.getFileType();

        // Attribution de l'URL du fichier au message en fonction du type
        if ("image".equals(fileType)) {
            message.setImage(fileUrl);
        } else if ("video".equals(fileType)) {
            message.setVideo(fileUrl);
        } else if ("audio".equals(fileType)) {
            message.setAudio(fileUrl);
        }

        // Envoi du message à travers le service messageService
        Message msg = messageService.sendNewMessageToGroup(message, groupe);

        // Vérification si le message retourné est null
        if (msg == null) {
            logger.error("sendMessage returned null");
            return;
        }
        System.out.println("Sending message to group " + groupId);

        // Envoi d'une notification à chaque destinataire du message s'il y en a

        messagingTemplate.convertAndSend("/topic/group/" + groupId,
                new GroupNotification(
                        msg.getId().toString(),
                        groupId.toString(),
                        msg.getSender().getId().toString(),
                        msg.getSender().getUsername(),
                        msg.getSender().getNom(),
                        msg.getSender().getPrenom(),
                        msg.getSender().getEmail(),
                        msg.getSender().getImage(),
                        msg.getImage(),
                        msg.getVideo(),
                        msg.getAudio(),
                        msg.getContent(),
                        msg.getTime().toString()
                ));


    }

    @MessageMapping("/typing/{username}")
    public void sendTyping(@DestinationVariable String username, @Payload TypingEvent typingEvent) {
        // Envoyer l'événement de frappe aux utilisateurs spécifiques
        messagingTemplate.convertAndSendToUser(username, "/queue/typing",  typingEvent);
    }



    @MessageMapping("/typingGroup/{groupId}/{username}")
    public void sendTyping(@DestinationVariable Long groupId, @DestinationVariable String username, @Payload TypingGroupEvent typingEvent) {
        System.out.println("Received typing event: " + typingEvent.getUsername() + " is typing: " + typingEvent.isTyping() + " in group " + typingEvent.getGroupId());
        messagingTemplate.convertAndSend("/topic/typing/" + groupId, typingEvent);
    }

    @MessageMapping("/view/{recipient}")
    public void sendViewStatus(@DestinationVariable String recipient, ViewEvent viewEvent) {
        // Envoyer l'événement de statut de vue à l'utilisateur spécifique
        messagingTemplate.convertAndSendToUser(recipient, "/queue/view", viewEvent);
        messageService.markMessageAsRead(viewEvent.getMessageId());
    }

    public boolean isUserActiveInChat(Long chatId, String username) {
        Set<String> usersInChat = activeUsers.get(chatId);
        return usersInChat != null && usersInChat.contains(username);
    }

   /* public void userJoinedChat(Long chatId, String username) {
        activeUsers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(username);
    }

    public void userLeftChat(Long chatId, String username) {
        Set<String> usersInChat = activeUsers.get(chatId);
        if (usersInChat != null) {
            usersInChat.remove(username);
            if (usersInChat.isEmpty()) {
                activeUsers.remove(chatId);
            }
        }
    }

    private void notifyChatMembers(Long chatId) {
        Set<String> activeUsersInChat = activeUsers.getOrDefault(chatId, ConcurrentHashMap.newKeySet());
        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/activeUsers", activeUsersInChat);
    }*/
   @MessageMapping("/{chatId}/user/{username}/join")
   public void userJoinedChat(@DestinationVariable Long chatId, @DestinationVariable String username) {
       activeUsers.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(username);
       userChatMap.put(username, chatId);
       messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/activeUsers", activeUsers.get(chatId));
   }

    @MessageMapping("/user/{username}/leave")
    public void userLeftChat(@DestinationVariable String username) {
        Long chatId = userChatMap.remove(username);
        if (chatId != null) {
            Set<String> users = activeUsers.get(chatId);
            if (users != null) {
                users.remove(username);
                if (users.isEmpty()) {
                    activeUsers.remove(chatId);
                } else {
                    messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/activeUsers", users);
                }
            }
        }
    }

    @MessageMapping("/{chatId}/user/{username}/active")
    @SendToUser("/queue/active")
    public Set<String> isUserActiveInChat(@DestinationVariable Long chatId) {
        return activeUsers.getOrDefault(chatId, Collections.emptySet());
    }

    public void notifyUnreadMessageCount(String username, Long chatId, int unreadCount) {
        String destination = "/queue/new-message";
        messagingTemplate.convertAndSendToUser(username, destination, new UnreadMessageNotification(chatId, unreadCount));
    }

    /* @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/all")
    public Message sendMessage(Message message) {
        return messageService.saveMessage(message);
    }
   @MessageMapping("/topic.onlineUser")
   @SendTo("/topic/messages")
   public User onlineUser(
         User user
   ) {
       userService.connect(user);
       return user;
   }
*/


    @GetMapping("/chats")
    public List<Chat> getAllChats() {
        return chatService.findAllChats();
    }
    @GetMapping("/chat/member/{userId}")
    public ResponseEntity<List<Chat>> getChatsForMember(@PathVariable Long userId) {
        User member = new User(); // Supposons que vous avez une manière de récupérer l'objet User ici
        member.setId(userId); // Mettez à jour selon votre implémentation
        List<Chat> chats = chatService.findChatsByMember(member);
        return ResponseEntity.ok(chats);
    }
    @GetMapping("/members/{chatId}")
    public ResponseEntity<List<User>> getChatMembers(@PathVariable Long chatId) {
        List<User> members = chatService.findMembresByChatId(chatId);
        return ResponseEntity.ok(members);
    }
    @GetMapping("/messages/{chatId}")
    public ResponseEntity<List<Message>> getChatMessages(@PathVariable Long chatId) {
        List<Message> messages = chatService.findMessagesByChat(chatId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/common-chats/{memberId1}/{memberId2}")
    public List<Chat> getCommonChats(@PathVariable  Long memberId1, @PathVariable  Long memberId2) {
        User member1 = userService.findById(memberId1).orElseThrow(() -> new RuntimeException("User not found"));
        User member2 = userService.findById(memberId2).orElseThrow(() -> new RuntimeException("User not found"));
        return chatService.getCommonChats(member1, member2);
    }
    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            List<String> folderNames = Arrays.asList("ImageChat"); // Update with actual username logic
            String fileUrl = firebaseStorageService.upload(file, folderNames);

            return ResponseEntity.ok(new FileUploadResponse(fileUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }
    }

    public static class FileUploadResponse {
        private String fileUrl;

        public FileUploadResponse(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getFileUrl() {
            return fileUrl;
        }
    }
    @PatchMapping("/chat/chats/mark-all-read/{chatId}")
    public ResponseEntity<Void> markAllChatMessagesRead(@PathVariable Long chatId) {
        // Implémentez la logique pour marquer tous les messages du chat comme lus
        chatService.markAllChatMessagesRead(chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count/{chatId}/{username}")
    public Long getUnreadMessageCount(@PathVariable Long chatId, @PathVariable String username) {

        return chatService.countUnreadMessagesByChatAndUser(chatId, username);
    }

    @GetMapping("/unread-total/{username}")
      public Long  getTotalUnreadMessageForUser(@PathVariable String username)
    {
        return chatService.countUnreadMessagesForUser(username);
    }
    @PatchMapping("/messages/mark-read/{chatId}/{username}")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long chatId, @PathVariable String username) {
        chatService.markMessagesAsRead(chatId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        chatService.deleteChat(id);
        messagingTemplate.convertAndSend("/topic/chat-deleted", id);
        return ResponseEntity.noContent().build();
    }

}
