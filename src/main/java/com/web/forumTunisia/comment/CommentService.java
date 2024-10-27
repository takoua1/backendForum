package com.web.forumTunisia.comment;


import com.web.forumTunisia.firebase.FirebaseStorageService;
import com.web.forumTunisia.interaction.Interaction;
import com.web.forumTunisia.interaction.InteractionRepository;
import com.web.forumTunisia.notification.Notification;
import com.web.forumTunisia.notification.NotificationRepository;
import com.web.forumTunisia.poste.Poste;
import com.web.forumTunisia.poste.PosteRepository;
import com.web.forumTunisia.poste.PosteService;
import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CommentService {

    public final CommentRepository commentRepository;
    public final PosteRepository posteRepository;
    public final UserRepository userRepository;
    public final FirebaseStorageService firebaseStorageService;
    private final NotificationRepository notifRepository;
    private  final InteractionRepository interRepository;

    public Comment addCommentToPoste(Comment comm, Poste poste, User user){

        boolean exist = userRepository.existsById(user.getId());
        if(exist)
        {
            comm.setPoste(poste);
            comm.setUser(user);
            comm.setDateCreate(new Date());
            return commentRepository.save(comm);
        }
        return null;
    }

    public ResponseEntity<Comment> addCommentToPosteWithImage(Comment comment , MultipartFile file , Long idUser, Long idPoste){
        try {
            User user = userRepository.findById(idUser).orElseThrow(() -> new RuntimeException("User not found"));
            Poste poste =posteRepository.findById(idPoste).orElseThrow(() -> new RuntimeException("Poste not found"));
            comment.setUser(user);
            comment.setPoste(poste);

            if(!file.isEmpty() && file !=null) {
                List<String> folderNames = Arrays.asList("ImageComment",comment.getUser().getUsername());
                String  imageUrl= firebaseStorageService.upload(file,folderNames);
                comment.setImage(imageUrl);
            }
            else {
                String imageUrl="";
                comment.setImage(imageUrl);
            }
            Date dateDeCreation = new Date();
            comment.setDateCreate(dateDeCreation);
            commentRepository.save(comment);
            return new ResponseEntity<>(comment, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }}

    public Comment addCommentToComment(Comment commParent, Comment commChilds,User user)
    {

        boolean exist =commentRepository.existsById(commParent.getId());
        boolean existUser = userRepository.existsById(user.getId());
        if(exist && existUser )
        {
            System.out.printf("poste child de comment",commParent.getPoste());
           commChilds.setParentComment(commParent);
           commChilds.setUser(user);
           commChilds.setDateCreate(new Date());
           commParent.getChildComments().add( commChilds);
            return commentRepository.save(commChilds);
        }
        return null;
    }
    @Transactional
    public ResponseEntity<Comment> addCommentToCommentWithImage(Comment commChilds , MultipartFile file , Long idUser, Long idCom){
        try {
            User user = userRepository.findById(idUser).orElseThrow(() -> new RuntimeException("User not found"));
            Comment commParent =commentRepository.findById(idCom).orElseThrow(() -> new RuntimeException("Comment not found"));
            commChilds.setUser(user);
            commChilds.setParentComment(commParent);

            if(!file.isEmpty() && file !=null) {
                List<String> folderNames = Arrays.asList("ImageCommentChild", commChilds.getUser().getUsername());
                String  imageUrl= firebaseStorageService.upload(file,folderNames);
                commChilds.setImage(imageUrl);
            }
            else {
                String imageUrl="";
                commChilds.setImage(imageUrl);
            }
            Date dateDeCreation = new Date();
            commChilds.setDateCreate(dateDeCreation);
            commentRepository.save(commChilds);
            return new ResponseEntity<>(commChilds, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public Poste findPosteByCommentId(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        return findPosteRecursively(comment);
    }

    private Poste findPosteRecursively(Comment comment) {
        if (comment == null) {
            return null;
        }

        // Récupère le poste du commentaire actuel
        Poste poste = commentRepository.findPosteByCommentId(comment.getId());

        if (poste != null) {
            return poste;
        } else {
            // Si le poste est null, récupère le parent et continue récursivement
            Comment parentComment = commentRepository.findParentCommentByCommentId(comment.getId());
            return findPosteRecursively(parentComment);
        }
    }


    public Comment findParentCommentWithPoste(Long childCommentId) {
        Comment childComment = commentRepository.findById(childCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire enfant non trouvé avec ID: " + childCommentId));

        return findParentWithPoste(childComment);
    }

    private Comment findParentWithPoste(Comment comment) {
        if (comment == null) {
            return null;
        }

        // Récupère le poste du commentaire actuel
        Poste poste = comment.getPoste();

        if (poste != null) {
            return comment; // Retourne le commentaire actuel s'il a un poste associé
        } else {
            // Si le poste est null, récupère le parent et continue récursivement
            return findParentWithPoste(comment.getParentComment());
        }
    }

  public Comment deleteComment(Long id){

     Boolean exist =commentRepository.existsById(id);
     Comment comment = commentRepository.getReferenceById(id);
      if(exist)
      {
          commentRepository.delete(comment);
          return comment;
      }
      return null;
  }
    public int getTotalLikes(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        return optionalComment.map(comment -> comment.getInteractions()
                .stream()
                .mapToInt(Interaction::getLike)
                .sum()).orElse(0);
    }
    public int getTotalDislikes(Long commentId) {
        Optional<Comment> optionalComment = commentRepository.findById(commentId);
        return optionalComment.map(comment -> comment.getInteractions()
                .stream()
                .mapToInt(Interaction::getDislike)
                .sum()).orElse(0);
    }

    public Comment getCommentByNotification(Notification notification) {
        return commentRepository.findById(notification.getComment().getId()).orElse(null);
    }


    @Transactional
    public void disableComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setEnabled(false);

        // Désactiver les notifications liées au commentaire
        List<Notification> notifications = notifRepository.findByComment(comment);
        for (Notification notification : notifications) {
            notification.setEnabled(false);
            notifRepository.save(notification);
        }
        List<Interaction> interactions = interRepository.findByComment(comment);
        for (Interaction interaction : interactions) {
            List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
            for (Notification interNotification : interNotifications) {
                interNotification.setEnabled(false);
                notifRepository.save(interNotification);
            }
        }


        commentRepository.save(comment);
    }

    private void disableChildComments(Comment parentComment) {
        List<Comment> childComments = parentComment.getChildComments();
        for (Comment childComment : childComments) {


            // Désactiver les notifications liées aux sous-commentaires
            List<Notification> childNotifications = notifRepository.findByComment(childComment);
            for (Notification notification : childNotifications) {
                notification.setEnabled(false);
                notifRepository.save(notification);
            }
            List<Interaction> interactions = interRepository.findByComment(childComment);
            for (Interaction interaction : interactions) {
                List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
                for (Notification interNotification : interNotifications) {
                    interNotification.setEnabled(false);
                    notifRepository.save(interNotification);
                }
            }
            // Récursion pour désactiver les sous-commentaires des sous-commentaires
            disableChildComments(childComment);
        }
    }
    @Transactional
    public void enableComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setEnabled(true);

        // Activer les notifications liées au commentaire
        List<Notification> notifications = notifRepository.findByComment(comment);
        for (Notification notification : notifications) {
            notification.setEnabled(true);
            notifRepository.save(notification);
        }
        List<Interaction> interactions = interRepository.findByComment(comment);
        for (Interaction interaction : interactions) {
            List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
            for (Notification interNotification : interNotifications) {
                interNotification.setEnabled(true);
                notifRepository.save(interNotification);
            }
        }

        // Activer les sous-commentaires si nécessaire


        commentRepository.save(comment);
    }

    private void enableChildComments(Comment parentComment) {
        List<Comment> childComments = parentComment.getChildComments();
        for (Comment childComment : childComments) {
            if (childComment.isEnabled()) { // Ne réactiver que si le commentaire est marqué comme activable


                // Activer les notifications liées aux sous-commentaires
                List<Notification> childNotifications = notifRepository.findByComment(childComment);
                for (Notification notification : childNotifications) {
                    notification.setEnabled(true);
                    notifRepository.save(notification);
                }
                List<Interaction> interactions = interRepository.findByComment(childComment);
                for (Interaction interaction : interactions) {
                    List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
                    for (Notification interNotification : interNotifications) {
                        interNotification.setEnabled(true);
                        notifRepository.save(interNotification);
                    }
                }

                // Récursion pour activer les sous-commentaires des sous-commentaires
                enableChildComments(childComment);
            }
        }
    }

    public Comment findCommentById(Long id)
    {
        return commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));

    }
    public List<Comment> getCommentsByPostId(Long postId) {

        Poste poste = posteRepository.findById(postId).orElseThrow(() -> new RuntimeException("Poste not found"));
        return commentRepository.findByPoste(poste);
    }
    public List<Comment> getChildComments(Long parentId) {
        // Récupérer tous les sous-commentaires activés pour un commentaire parent
        return commentRepository.findEnabledChildCommentsByParentId(parentId);
    }
    public Comment getCommentWithParent(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Ici, vous pouvez vérifier et charger le parent si nécessaire
        // Cela suppose que le parentComment est déjà chargé en mode EAGER
        return comment.getParentComment();
    }
    }



