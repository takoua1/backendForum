package com.web.forumTunisia.user;

import com.web.forumTunisia.chat.Chat;
import com.web.forumTunisia.chat.ChatRepository;
import com.web.forumTunisia.firebase.FirebaseStorageService;
import com.web.forumTunisia.groupe.Groupe;
import com.web.forumTunisia.groupe.GroupeRepository;
import com.web.forumTunisia.message.Message;
import com.web.forumTunisia.message.MessageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Session;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseStorageService firebaseStorageService;
    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final GroupeRepository groupeRepository;


    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }

        // update the password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // save the new password
        userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {

        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {

        return userRepository.findById(id);
    }

    public List<User> findAllRoles() {
        return userRepository.findAllByRole(Role.USER,Sort.by(Sort.Direction.DESC, "dateAuthenticated"));
    }

    public User updateUser(Long id, User user) {
        boolean exist = userRepository.existsById(id);

        if (exist) {
            User u = userRepository.getReferenceById(id);

            u.setUsername(user.getUsername());
            u.setNom(user.getNom());
            u.setPrenom(user.getPrenom());
            u.setEmail(user.getEmail());
            u.setBio(user.getBio());
            u.setEmploi(user.getEmploi());
            u.setAdresse(user.getAdresse());
            u.setPays(user.getPays());
            u.setEducation(user.getEducation());
            u.setCompteFacebook(user.getCompteFacebook());
            u.setCompteTwitter(user.getCompteTwitter());
            u.setCompteLinked(user.getCompteLinked());
            u.setCompteInstagram(user.getCompteInstagram());
            u.setTel(user.getTel());

            userRepository.save(u);
            return u;
        }
        return null;
    }

    @Transactional
    public User updateUserImage(Long id, MultipartFile multipartFile) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found for userId: " + id));

        List<String> folderNames = Arrays.asList("ImageProfile", user.getUsername());
        String imageUrl = firebaseStorageService.upload(multipartFile, folderNames);
        if (imageUrl != null) {
            user.setImage(imageUrl);
        }
        // Sauvegarder les modifications dans la base de données
        User u = userRepository.save(user);
        return u;
    }

    public void connect(User user) {
        var storedUser = userRepository.findById(user.getId()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatus(Status.CONNECTE);

            userRepository.save(storedUser);
        }
    }

    public void disconnect(User user) {
        var storedUser = userRepository.findById(user.getId()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatus(Status.DECONNECTE);
            userRepository.save(storedUser);
        }
    }

    public void updateLastActive(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastActive(LocalDateTime.now());
            user.setStatus(Status.CONNECTE);
            userRepository.save(user);
        } else {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }

    @Transactional
    public void logout(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastActive(LocalDateTime.now());
            user.setStatus(Status.DECONNECTE);
            userRepository.save(user);
        }
    }

    public void checkInactiveUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1); // Define the inactivity threshold
        List<User> inactiveUsers = userRepository.findByLastActiveBefore(threshold);
        for (User user : inactiveUsers) {
            user.setStatus(Status.DECONNECTE);
            userRepository.save(user);
        }
    }

    public void updateStatusToDisconnected(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            user.setStatus(Status.DECONNECTE);
            userRepository.save(user);
        }
    }

    public String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (days > 3) {
            return null;
        } else if (days > 0) {
            return days + " jour" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return String.format("%d heure%s", hours, (hours > 1 ? "s" : ""));
        } else if (minutes > 0) {
            return String.format("%d minute%s", minutes, (minutes > 1 ? "s" : ""));
        } else {
            return String.format("%d seconde%s", seconds, (seconds > 1 ? "s" : ""));
        }
    }

    public Duration getOfflineDuration(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getStatus() == Status.DECONNECTE) {
                LocalDateTime now = LocalDateTime.now();
                return Duration.between(user.getLastActive(), now);
            }
        }
        return Duration.ZERO;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
  /*  @Transactional
    public void deleteUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Retirer l'utilisateur des chats auxquels il appartient
        for (Chat chat : user.getChats()) {
            List<User> allMembers = new ArrayList<>(chat.getMembres()); // Créez une copie pour éviter ConcurrentModificationException
            if (allMembers.remove(user)) { // Assurez-vous que l'utilisateur est bien supprimé
                chat.setMembres(allMembers);

                // Enregistrez les modifications pour les chats privés
                if ("privée".equals(chat.getTypeChat().toString())) {
                    chatRepository.save(chat);
                }
                // Enregistrez les modifications pour les chats de groupe
                else if ("groupe".equals(chat.getTypeChat().toString())) {
                    Groupe groupe = chat.getGroupe();
                    if (groupe != null) {
                        List<User> groupeMembers = new ArrayList<>(groupe.getChat().getMembres());
                        if (groupeMembers.remove(user)) {
                            groupe.getChat().setMembres(groupeMembers); // Met à jour les membres du chat associé
                            if (groupeMembers.isEmpty()) {
                                groupeRepository.delete(groupe);
                            } else {
                                groupeRepository.save(groupe);
                            }
                        }
                    }
                    chatRepository.save(chat);
                }
            }
        }

        // Supprimer l'utilisateur après avoir mis à jour les chats et groupes
        userRepository.delete(user);
    }
*/
 /* @Transactional
  public void deleteUserById(Long userId) {
      User user = userRepository.findById(userId)
              .orElseThrow(() -> new EntityNotFoundException("User not found"));

      // Retirer l'utilisateur des chats auxquels il appartient
      for (Chat chat : user.getChats()) {
          List<User> allMembers = new ArrayList<>(chat.getMembres());
          if (allMembers.remove(user)) {
              chat.setMembres(allMembers);

              // Enregistrez les modifications pour les chats privés
              if ("privée".equals(chat.getTypeChat().toString())) {
                  chatRepository.save(chat);
              }
              // Enregistrez les modifications pour les chats de groupe
              else if ("groupe".equals(chat.getTypeChat().toString())) {
                  Groupe groupe = chat.getGroupe();
                  if (groupe != null) {
                      List<User> groupeMembers = new ArrayList<>(groupe.getChat().getMembres());
                      if (groupeMembers.remove(user)) {
                          // Met à jour les membres du groupe
                          groupe.getChat().setMembres(groupeMembers);

                          // Vérifier si l'utilisateur supprimé est le créateur du groupe
                          if (groupe.getUserCreature().equals(user)) {
                              List<User> membres = groupe.getChat().getMembres();
                              User newCreator = membres.stream().filter(u -> !u.equals(user)).findFirst().orElse(null);

                              if (newCreator != null) {
                                  groupe.setUserCreature(newCreator);
                                  groupeRepository.save(groupe);
                              } else {
                                  // Si aucun autre membre n'est trouvé, supprimer le groupe
                                  groupeRepository.delete(groupe);
                              }
                          } else {
                              // Enregistrer le groupe si le créateur n'est pas l'utilisateur supprimé
                              groupeRepository.save(groupe);
                          }
                      }
                  }
                  chatRepository.save(chat);
              }
          }
      }

      // Supprimer l'utilisateur après avoir mis à jour les chats et groupes
      userRepository.delete(user);
  }
*/

    @Transactional
    public void deleteUserById(Long userId) {
        // Trouver l'utilisateur à supprimer
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Supprimer les messages envoyés par l'utilisateur
        messageRepository.deleteBySenderId(userId);

        // Supprimer les messages reçus par l'utilisateur
        List<Message> receivedMessages = messageRepository.findByReceiversId(userId);
        for (Message message : receivedMessages) {
            message.getReceivers().remove(user);
            messageRepository.save(message);
        }

        // Liste des chats à supprimer après traitement
        List<Chat> chatsToDelete = new ArrayList<>();

        // Gérer les chats associés à l'utilisateur
        for (Chat chat : user.getChats()) {
            if (chat != null) {
                chat.getMembres().remove(user);
                if (chat.getMembres().isEmpty()) {
                    chatsToDelete.add(chat);
                } else if ("privée".equals(chat.getTypeChat().toString())) {
                    chatRepository.save(chat);
                }
            }
        }

        // Gérer les groupes créés par l'utilisateur
        List<Groupe> groupes = groupeRepository.findByUserCreature(user);
        for (Groupe groupe : groupes) {
            if (groupe != null) {
                Chat chat = groupe.getChat();
                if (chat != null) {
                    chat.getMembres().remove(user);
                    chatRepository.save(chat);

                    // Gérer le cas où l'utilisateur supprimé est le créateur du groupe
                    if (groupe.getUserCreature() != null && groupe.getUserCreature().equals(user)) {
                        // Assigner un nouveau créateur
                        User newCreator = chat.getMembres().stream().findFirst().orElse(null);
                        if (newCreator != null) {
                            groupe.setUserCreature(newCreator);
                            groupeRepository.save(groupe);
                        } else {
                            // Si aucun autre créateur, préparer le groupe et le chat pour suppression
                            groupe.setChat(null);
                            groupeRepository.save(groupe);
                            chatsToDelete.add(chat);
                            groupeRepository.delete(groupe);
                        }
                    }
                }
            }
        }

        // Supprimer les chats restants après traitement
        for (Chat chat : chatsToDelete) {
            chatRepository.delete(chat);
        }

        // Dissocier les groupes avant la suppression de l'utilisateur
        user.getChats().clear();
        user.getGroupes().clear();
        userRepository.save(user);

        // Supprimer l'utilisateur
        userRepository.delete(user);
    }
    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(false);

        // Gérer les groupes créés par l'utilisateur
        List<Groupe> groupes = groupeRepository.findByUserCreature(user);
        for (Groupe groupe : groupes) {
            if (groupe != null) {
                Chat chat = groupe.getChat();
                if (chat != null) {
                    chat.getMembres().remove(user);
                    chatRepository.save(chat);

                    // Gérer le cas où l'utilisateur supprimé est le créateur du groupe
                    if (groupe.getUserCreature() != null && groupe.getUserCreature().equals(user)) {
                        // Assigner un nouveau créateur
                        User newCreator = chat.getMembres().stream().findFirst().orElse(null);
                        if (newCreator != null) {
                            groupe.setUserCreature(newCreator);
                            groupeRepository.save(groupe);
                        }
                    }
                }
            }
        }






        userRepository.save(user);
    }

    public void unblockUser(Long userId)
    {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
