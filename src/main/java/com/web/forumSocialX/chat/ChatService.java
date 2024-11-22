package com.web.forumSocialX.chat;


import com.web.forumSocialX.message.Message;
import com.web.forumSocialX.message.MessageRepository;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private Map<Long, Map<String, Boolean>> chatActiveUsers = new HashMap<>();
    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }
    public List<Chat> findAllChats() {
        return chatRepository.findAll();
    }
    public List<Chat> findChatsByMember(User member) {
        return chatRepository.findByMembre(member, Sort.by(Sort.Direction.DESC, "lastMessage"));
    }

    public void userJoinedChat(Long chatId, String username) {
        chatActiveUsers.putIfAbsent(chatId, new HashMap<>());
        chatActiveUsers.get(chatId).put(username, true);
    }

    public boolean isUserActiveInChat(Long chatId, String username) {
        return chatActiveUsers.getOrDefault(chatId, new HashMap<>()).getOrDefault(username, false);
    }

    public void userLeftChat(Long chatId, String username) {
        if (chatActiveUsers.containsKey(chatId)) {
            chatActiveUsers.get(chatId).put(username, false);
        }
    }


 public List<User>   getChatMembers(Long chatId){
        Chat chat=chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getMembres();

    }

    public List<User> findMembresByChatId(Long chatId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getMembres();
    }
    public List<Message> findMessagesByChat(Long chatId)
    {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getMessages()
            .stream()
            .sorted(Comparator.comparing(Message::getTime))
            .collect(Collectors.toList());
    }
    public List<Chat> getCommonChats(User member1, User member2) {
        return chatRepository.findCommonChatsByMembers(member1, member2);
    }


    public void markAllChatMessagesRead(Long chatId) {
        List<Message> messages = messageRepository.findByChatId(chatId);
        for (Message message : messages) {
            message.setRead(true); // Marquer le message comme lu
        }
        messageRepository.saveAll(messages); // Enregistrer les modifications dans la base de données
    }


    public void markMessagesAsRead(Long chatId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        List<Message> unreadMessages = messageRepository.findUnreadMessagesByChatAndUser(chatId, user);
        for (Message message : unreadMessages) {
            message.setRead(true);
            messageRepository.save(message);
        }
    }
    public Long countUnreadMessagesByChatAndUser(Long chatId, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return messageRepository.countUnreadMessagesByChatAndUser(chatId, user);
    }

    public Long countUnreadMessagesForUser(String username)

    { User user = userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return messageRepository.countUnreadMessagesForUser(user);
    }
   /* public void sendMessage(User utilisateur, List<User> destinataires, String contenu, Chat chat) {
        Message message = new Message();
        message.setContent(contenu);
        message.setDate(new Date());
        message.setUtilisateur(utilisateur);
        message.setDestinataires(destinataires);
        message.setConversation(conversation);
        messageRepository.save(message);
    }

  /*  public Chat addParticipantToChat(Chat chat, User user) {
        // Trouver le chat par son ID
        boolean exist = chatRepository.existsById(chat.getId());

        if (exist) {
            List<User> participants = new ArrayList<>(chat.getParticipants());
            if (!participants.contains(user)) {
                participants.add(user);
                chat.setParticipants(participants);
           return     chatRepository.save(chat);
            }
        }
        return chat;

    }
   public Chat removeParticipantFromChat(Chat chat, User user) {
        boolean exist = chatRepository.existsById(chat.getId());

        if (exist) {
            List<User> participants = new ArrayList<>(chat.getParticipants());
            participants.removeIf(participant -> participant.equals(user));

            chat.setParticipants(participants);


            return  chatRepository.save(chat);
        }
        return chat;

    }*/
   public void deleteChat(Long chatId) {
       // Récupérer le chat par son ID
       Optional<Chat> chatOptional = chatRepository.findById(chatId);

       if (chatOptional.isPresent()) {
           Chat chat = chatOptional.get();

           // Supprimer tous les messages associés à ce chat
           List<Message> messages = messageRepository.findByChatId(chat.getId());
           if (!messages.isEmpty()) {
               messageRepository.deleteAll(messages);  // Supprimer tous les messages
               System.out.println("Messages supprimés.");
           }

           // Supprimer tous les membres associés à ce chat (si applicable)
           if (chat.getMembres() != null && !chat.getMembres().isEmpty()) {
               chat.getMembres().clear();  // Supprimer les membres de la liste
               chatRepository.save(chat);  // Sauvegarder le chat sans les membres
               System.out.println("Membres du chat supprimés.");
           }

           // Supprimer le chat après avoir vidé les messages et les membres
           chatRepository.deleteById(chat.getId());
           System.out.println("Chat supprimé.");
       } else {
           System.out.println("Chat non trouvé avec l'ID spécifié.");
       }
   }
}
