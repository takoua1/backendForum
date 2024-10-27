package com.web.forumTunisia.message;


import com.web.forumTunisia.chat.*;
import com.web.forumTunisia.firebase.FirebaseStorageService;
import com.web.forumTunisia.groupe.Groupe;
import com.web.forumTunisia.groupe.GroupeRepository;
import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class MessageService {
    private final GroupeRepository groupeRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
   private final ChatRepository chatRepository;
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);



    public List<Message> findAllMessages() {
        return messageRepository.findAll();
    }
  /*  public List<Message> findMessagesBetweenUsers(Long senderId, Long receiversId) {
        // Fetch all messages sent by the sender
        List<Message> messages = messageRepository.findBySenderId(senderId);
  if  (!messages.isEmpty())
        // Filter messages to find those where the receivers include the specified user
  {  return messages.stream()
                .filter(message -> message.getReceivers().stream()
                        .anyMatch(receiver -> receiver.getId().equals(receiversId)))

                .collect(Collectors.toList());}
  else{
        List<Message> messags = messageRepository.findBySenderId(receiversId);
      if  (!messags.isEmpty())
      // Filter messages to find those where the receivers include the specified user
      {  return messags.stream()
              .filter(message -> message.getSender()
                      .anyMatch(receiver -> receiver.getId().equals(senderId)))

              .collect(Collectors.toList());}
  }
        return Collections.emptyList();

    }
  public List<Message> findMessagesBetweenUsers(Long senderId, Long receiversId) {
      // Trier par ordre décroissant pour afficher les messages les plus récents en premier
      List<Message> messages = messageRepository.findBySenderId(senderId).stream()
              .sorted((m1, m2) -> m2.getTime().compareTo(m1.getTime()))
              .collect(Collectors.toList());

      // Filtrer les messages pour inclure seulement ceux où le récepteur est le destinataire recherché
      return messages.stream()
              .filter(message -> message.getReceivers().stream()
                      .anyMatch(receiver -> receiver.getId().equals(receiversId)))
              .collect(Collectors.toList());
  }*/
  /*public List<Message> findMessagesBetweenUsers(Long senderId, Long receiversId) {
      // Rechercher les messages envoyés par l'expéditeur
      List<Message> messages = messageRepository.findBySenderId(senderId);

      if (!messages.isEmpty()) {
          // Filtrer les messages pour inclure seulement ceux où le récepteur est le destinataire recherché et le type de chat est "privée"
          return messages.stream()
                  .filter(message -> message.getReceivers().stream()
                          .anyMatch(receiver -> receiver.getId().equals(receiversId)) &&
                          "privée".equals(message.getChat().getTypeChat().toString()))
                  .collect(Collectors.toList());
      } else {
          // Rechercher les messages envoyés par le récepteur
          List<Message> messags = messageRepository.findBySenderId(receiversId);
          if (!messags.isEmpty()) {
              // Filtrer les messages pour inclure seulement ceux où le récepteur est l'expéditeur recherché et le type de chat est "privée"
              return messags.stream()
                      .filter(message -> message.getReceivers().stream()
                              .anyMatch(receiver -> receiver.getId().equals(senderId)) &&
                              "privée".equals(message.getChat().getTypeChat().toString()))
                      .collect(Collectors.toList());
          }
      }
      return Collections.emptyList();
  }*/
  public List<Message> findMessagesBetweenUsers(Long senderId, Long receiversId) {
      List<Message> messages = messageRepository.findBySenderId(senderId);
      messages.addAll(messageRepository.findBySenderId(receiversId));

      List<Message> privateMessages = messages.stream()
              .filter(message -> message.getReceivers().stream()
                      .anyMatch(receiver -> receiver.getId().equals(senderId) || receiver.getId().equals(receiversId))
                      && "privée".equals(message.getChat().getTypeChat().toString()))
              .collect(Collectors.toList());

      logger.info("Found {} private messages between users {} and {}", privateMessages.size(), senderId, receiversId);
      return privateMessages;
  }

    public Message sendMessage(Message message) {
        User sender = userRepository.findById(message.getSender().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur expéditeur non trouvé"));
        User receiver = userRepository.findById(message.getReceivers().get(0).getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur destinataire non trouvé"));

        List<Message> previousMessages = findMessagesBetweenUsers(sender.getId(), receiver.getId());
        logger.info("Found {} previous messages between {} and {}", previousMessages.size(), sender.getUsername(), receiver.getUsername());

        Date dateLaste = new Date();

        if (!previousMessages.isEmpty()) {
            // Si des messages précédents existent, récupérer le chat associé
            Chat existingChat = previousMessages.get(0).getChat();
            if (existingChat != null && "privée".equals(existingChat.getTypeChat().toString())) {
                logger.info("Using existing private chat between {} and {}", sender.getUsername(), receiver.getUsername());
                return sendMessageToExistingChat(existingChat, sender, receiver, dateLaste,
                        message.getContent(), message.getImage(), message.getVideo(), message.getAudio());
            } else {
                logger.info("No existing private chat found. Existing chat type: {}", existingChat != null ? existingChat.getTypeChat() : "null");
            }
        }

        logger.info("Creating new chat for message between {} and {}", sender.getUsername(), receiver.getUsername());
        // Si aucun message précédent n'existe, créer un nouveau chat
        Chat newChat = createNewChatIfNecessary(sender, dateLaste, message.getReceivers());
        return sendMessageToNewChat(newChat, sender, receiver, dateLaste,
                message.getContent(), message.getImage(), message.getVideo(), message.getAudio());
    }
    private Message sendMessageToExistingChat(Chat existingChat, User sender, User receiver, Date dateLaste, String content, String image, String video ,String audio) {

        Message message = new Message();
        message.setContent(content);
        message.setTime(new Date());
        message.setSender(sender);
        message.setReceivers(Collections.singletonList(receiver));
        message.setChat(existingChat);
        message.setImage(image);
        message.setVideo(video);
        message.setAudio(audio);
        message.setRead(false);
        existingChat.setLastMessage(dateLaste);

        existingChat.getMessages().add(message);
        chatRepository.save(existingChat);
        messageRepository.save(message);


        return message;
    }
    private Chat createNewChatIfNecessary(User sender,Date dateLaste  ,List<User> receivers) {
        Chat newChat = new Chat();
        newChat.setTypeChat(TypeChat.privée); // Supposons que vous avez une énumération TypeChat
      //  newChat.setSender(sender);

        // Fusionne la liste des membres avec le sender et les receivers
        List<User> allMembers = new ArrayList<>();
        allMembers.add(sender);
        allMembers.addAll(receivers);
        newChat.setLastMessage(dateLaste);
        newChat.setMembres(allMembers);

        // Pas de groupe pour un chat privé
        return chatRepository.save(newChat);
    }
    private Message sendMessageToNewChat(Chat newChat, User sender, User receiver,Date dateLaste, String content, String image,String video,String audio) {
        // Créer le message
        Message message = new Message();
        message.setContent(content);
        message.setTime(new Date());
        message.setSender(sender);
        message.setReceivers(Collections.singletonList(receiver));
        message.setChat(newChat);
        message.setImage(image);
        message.setVideo(video);
        message.setAudio(audio);
        message.setRead(false);

        newChat.getMessages().add(message);
        newChat.setLastMessage(dateLaste);
        chatRepository.save(newChat);

        messageRepository.save(message);

        return message;
    }
    public List<User> getReceiversByMessageId(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return message.getReceivers();
    }
    public Optional<Message> geMessageById(Long id)

    {
        return messageRepository.findById(id);
    }
    public List<Chat> getCommonChats(User sender1, User sender2) {
        return messageRepository.findCommonChats(sender1, sender2);
    }
    @Transactional
    public Message sendNewMessageToGroup(Message message, Groupe groupe) {
        try {
            System.out.println("Avant l'envoi, groupe: " + groupe);
            // Récupérer le chat associé au groupe
            Chat chat = chatRepository.findById(groupe.getChat().getId()).get();

            // Créer et configurer le nouveau message
            Message newMessage = new Message();
            newMessage.setSender(message.getSender());
            newMessage.setContent(message.getContent());
            newMessage.setImage(message.getImage());
            newMessage.setVideo(message.getVideo());
            newMessage.setAudio(message.getAudio());
            newMessage.setRead(false);
            newMessage.setTime(new Date());
            newMessage.setChat(chat);

            // Récupérer les membres du groupe pour définir les destinataires
            List<User> receivers = new ArrayList<>(chat.getMembres());
            receivers.remove(message.getSender());
            newMessage.setReceivers(receivers);
            chat.getMessages().add(newMessage);
            Date dateLaste = new Date();
            chat.setLastMessage(dateLaste);
            Message savedMessage = messageRepository.save(newMessage);


            chatRepository.save(chat);




            return savedMessage;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'ajout d'un nouveau message dans le groupe", e);
        }
    }
    public void markMessageAsRead(Long messageId) {
        // Marquer un message spécifique comme lu
        Optional<Message> messageOptional = messageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            message.setRead(true);
            messageRepository.save(message);
        } else {
            // Gérer l'erreur si le message n'est pas trouvé
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
    }
    public void deleteMessage(Long id) {
        // Récupérer le message par son ID
        Optional<Message> messageOptional = messageRepository.findById(id);

        if (messageOptional.isPresent()) {
            Message message = messageOptional.get();
            Chat chat = message.getChat(); // Assurez-vous que Message a une relation avec Chat

            // Supprimer le message
            messageRepository.deleteById(id);

            // Vérifier si le chat contient encore des messages
            List<Message> remainingMessages = messageRepository.findByChatId(chat.getId());

            if (remainingMessages.isEmpty()) {

                chat.getMembres().clear();  // Supprimer les membres du chat

                // Sauvegarder le chat sans membres (si cette étape est nécessaire)
                chatRepository.save(chat);

                // Supprimer le chat après avoir vérifié qu'il est vide
                chatRepository.deleteById(chat.getId());
                System.out.println("Chat supprimé car vide.");
            } else {
                System.out.println("Chat non supprimé car il reste des messages.");
            }
        } else {
            System.out.println("Le message avec l'ID spécifié n'existe pas.");
        }

    }

    public Message findLastMessage() {
        // Supposez que vous ayez une méthode dans le repository pour trouver le dernier message
        return messageRepository.findTopByOrderByIdDesc(); // Remplacez par votre méthode de récupération
    }


}