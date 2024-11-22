package com.web.forumSocialX.groupe;

import com.web.forumSocialX.chat.Chat;
import com.web.forumSocialX.chat.ChatRepository;
import com.web.forumSocialX.chat.TypeChat;
import com.web.forumSocialX.firebase.FirebaseStorageService;
import com.web.forumSocialX.message.Message;
import com.web.forumSocialX.message.MessageRepository;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupeService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    public Groupe saveGroupe(Groupe groupe) {
        return groupeRepository.save(groupe);
    }
    public List<Groupe> findAllGroupes() {
        return groupeRepository.findAll();
    }
  /*  public Groupe addParticipantToGroupe(Groupe groupe, User user) {
        boolean exist = groupeRepository.existsById(groupe.getId());
        if (exist) {
            List<User> participants = new ArrayList<>(groupe.getParticipants());
            if (!participants.contains(user)) {
            participants.add(user);
            groupe.setParticipants(participants);
            return groupeRepository.save(groupe);}
        }
        return groupe;
    }
    public Groupe removeParticipantFromGroupe(Groupe groupe, User user) {
        boolean exist = groupeRepository.existsById(groupe.getId());

        if (exist) {
            List<User> participants = new ArrayList<>(groupe.getParticipants());
            participants.removeIf(participant -> participant.equals(user));

            groupe.setParticipants(participants);


          return  groupeRepository.save(groupe);
        }
       return groupe;

    }*/

    public ResponseEntity<Groupe> addGroupe(Groupe groupe, MultipartFile file, Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            groupe.setUserCreature(user);

            if (file != null && !file.isEmpty()) {
                List<String> folderNames = Arrays.asList("ImageGroupe", user.getUsername());
                String imageUrl = firebaseStorageService.upload(file, folderNames);
                groupe.setGroupImage(imageUrl);
            } else {
                groupe.setGroupImage(""); // No image URL
            }

            Date dateDeCreation = new Date();
            groupe.setDateCreate(dateDeCreation);

            // Création du chat pour le groupe
            Chat newChat = new Chat();
            newChat.setTypeChat(TypeChat.groupe);
            newChat.setLastMessage(dateDeCreation);
            newChat.setMembres(Collections.singletonList(user)); // Ajouter le créateur comme membre

            groupe.setChat(newChat);
            chatRepository.save(newChat);

            // Initialisation des dates de jonction des utilisateurs
            Map<Long, Date> userJoinDates = new HashMap<>();
            userJoinDates.put(user.getId(), dateDeCreation); // Ajouter le créateur avec la date actuelle
            groupe.setUserJoinDates(userJoinDates);

            // Sauvegarder le groupe
            groupeRepository.save(groupe);

            return new ResponseEntity<>(groupe, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); // Ajout d'une trace d'erreur pour plus de détails
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public List<Groupe> getGroupesForCurrentUser(Long id) {

        return groupeRepository.findByChat_Membres_Id(id);
    }
    public List<Groupe> findByUser(Long userId) {
        return groupeRepository.findByMembresId(userId,Sort.by(Sort.Direction.DESC, "chat.lastMessage"));
    }
    public ResponseEntity<Groupe> addMembre(Long groupeId, Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Groupe groupe = groupeRepository.findById(groupeId)
                    .orElseThrow(() -> new RuntimeException("Groupe not found"));

            Chat chat = groupe.getChat();
            List<User> allMembers = chat.getMembres();

            // Vérifiez si l'utilisateur est déjà membre pour éviter les doublons
            if (!allMembers.contains(user)) {
                allMembers.add(user);
                chat.setMembres(allMembers);

                // Mettre à jour les dates de jonction
                Map<Long, Date> userJoinDates = groupe.getUserJoinDates();
                if (userJoinDates == null) {
                    userJoinDates = new HashMap<>();
                }
                userJoinDates.put(userId, new Date()); // Ajout de la date actuelle pour le nouvel utilisateur
                groupe.setUserJoinDates(userJoinDates);

                chatRepository.save(chat);
                groupeRepository.save(groupe);
            }

            return new ResponseEntity<>(groupe, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Ajout d'une trace d'erreur pour plus de détails
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    public void deleteGroup(Long groupId) {
        // 1. Récupérer le groupe
        Optional<Groupe> groupOptional = groupeRepository.findById(groupId);
        if (groupOptional.isPresent()) {
            Groupe group = groupOptional.get();
            Chat chat = group.getChat(); // Relation One-to-One avec Chat
            group.getBlockedMembers().clear();
            // 2. Supprimer tous les messages dans le chat
            if (chat != null) {
                List<Message> messages = messageRepository.findByChatId(chat.getId());
                if (!messages.isEmpty()) {
                    messageRepository.deleteAll(messages);
                    System.out.println("Messages supprimés pour le chat: " + chat.getId());
                }

                // 3. Supprimer les membres du chat de manière appropriée
                for (User membre : new ArrayList<>(chat.getMembres())) {
                    chat.getMembres().remove(membre);

                    // Supprimer l'utilisateur de la map des dates de jointure
                    if (group.getUserJoinDates().containsKey(membre.getId())) {
                        group.getUserJoinDates().remove(membre.getId());
                        System.out.println("Utilisateur supprimé de la liste des dates de jointure: " + membre.getId());
                    } else {
                        System.out.println("L'utilisateur avec l'ID " + membre.getId() + " n'est pas dans la liste des membres du groupe");
                    }
                }

                // 4. Supprimer le chat
                chat.setGroupe(null); // Détacher le groupe du chat
                group.setChat(null);  // Détacher le chat du groupe
                chatRepository.save(chat);
                groupeRepository.save(group);
                chatRepository.delete(chat);

                System.out.println("Chat supprimé: " + chat.getId());
            }

            // 5. Supprimer le groupe
            groupeRepository.delete(group);

            System.out.println("Groupe et ses dépendances supprimés: " + group.getId());
        } else {
            System.out.println("Le groupe avec l'ID spécifié n'existe pas.");
        }
    }


@Transactional
    public void quitterGroupe(Long groupeId, Long userId) {
        System.out.println("Tentative de quitter le groupe: " + groupeId + " pour l'utilisateur: " + userId);

        Optional<Groupe> groupeOptional = groupeRepository.findById(groupeId);

        if (groupeOptional.isPresent()) {
            Groupe groupe = groupeOptional.get();
            Chat chat = groupe.getChat();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("Groupe trouvé: " + groupe.getId());
            if (groupe.getUserJoinDates().containsKey(userId)) {
                groupe.getUserJoinDates().remove(userId);
                System.out.println("Utilisateur supprimé de la liste des dates de jointure");
            } else {
                System.out.println("L'utilisateur n'est pas dans la liste des membres du groupe");
            }
            // Vérifiersi l'utilisateur est le créateur du groupe
            if (groupe.getUserCreature() != null && groupe.getUserCreature().equals(user)) {
                System.out.println("L'utilisateur est le créateur du groupe.");

                // Assigner un nouveau créateur
                User newCreator = chat.getMembres().stream()
                        .filter(membre -> !membre.equals(user)).findFirst().orElse(null);

                if (newCreator != null) {
                    // Nouveau créateur assigné
                    groupe.setUserCreature(newCreator);
                    groupeRepository.save(groupe);
                    System.out.println("Nouveau créateur assigné: " + newCreator.getId());
                } else {
                    // Si aucun autre membre n'est présent, supprimer le groupe
                    System.out.println("Aucun autre membre présent, suppression du groupe.");
                    deleteGroup(groupe.getId());
                    return;  // Terminer ici car le groupe est supprimé
                }


            }

            // Supprimer l'utilisateur des membres du chat
            chat.getMembres().remove(user);
            chatRepository.save(chat);
            System.out.println("Utilisateur retiré des membres du chat.");

            // Vérifier si le chat est maintenant vide
            if (chat.getMembres().isEmpty()) {
                System.out.println("Le chat est maintenant vide, suppression du groupe.");
                deleteGroup(groupe.getId());
            }
        } else {
            System.out.println("Le groupe n'existe pas.");
        }
    }

    public void blockMember(Long groupeId, String username) {
        // Récupérer le groupe
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        // Récupérer l'utilisateur à bloquer
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Ajouter l'utilisateur à la liste des membres bloqués
        if (!groupe.getBlockedMembers().contains(user)) {
            groupe.getBlockedMembers().add(user);
            groupe.getChat().getMembres().remove(user);
            groupeRepository.save(groupe);
            System.out.println("Utilisateur bloqué dans le groupe");
        } else {
            System.out.println("L'utilisateur est déjà bloqué.");
        }
    }

    public void unblockMember(Long groupeId, String username) {
        // Récupérer le groupe
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        // Récupérer l'utilisateur à débloquer
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Retirer l'utilisateur de la liste des membres bloqués
        if (groupe.getBlockedMembers().contains(user)) {
            groupe.getBlockedMembers().remove(user);
            groupeRepository.save(groupe);
            System.out.println("Utilisateur débloqué du groupe");
        } else {
            System.out.println("L'utilisateur n'est pas bloqué.");
        }
    }

   public void  deleteMember(Long groupeId,String username)
   {
       Groupe groupe = groupeRepository.findById(groupeId)
               .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

       // Récupérer l'utilisateur à débloquer
       User user = userRepository.findByUsername(username)
               .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
       if (groupe.getChat().getMembres().contains(user)) {
           groupe.getChat().getMembres().remove(user);

           groupeRepository.save(groupe);
           System.out.println("Utilisateur est supprimé dans le groupe");
       } else {
           System.out.println("L'utilisateur n'est pas supprimé  de groupe.");
       }
   }
   public void getAdmin(Long groupeId,String username)
   {
       Groupe groupe = groupeRepository.findById(groupeId)
               .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

       // Récupérer l'utilisateur à débloquer
       User user = userRepository.findByUsername(username)
               .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
       groupe.setUserCreature(user);
       groupeRepository.save(groupe);
       System.out.println("Nouveau créateur assigné: " + user.getId());

   }

    public Groupe updateGroupe(Long groupeId, MultipartFile file, Groupe newGroupe) {
        // Vérifier si le groupe existe
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new EntityNotFoundException("Groupe non trouvé"));

        // Mettre à jour les champs du groupe
        groupe.setGroupName(newGroupe.getGroupName());
        groupe.setCategory(newGroupe.getCategory());

        // Gestion de l'image
        if (file != null && !file.isEmpty()) {
            List<String> folderNames = Arrays.asList("ImageGroupe", groupe.getUserCreature().getUsername());
            String imageUrl = firebaseStorageService.upload(file, folderNames);
            groupe.setGroupImage(imageUrl);
        }

        // Sauvegarder le groupe mis à jour
        return groupeRepository.save(groupe);
    }

}
