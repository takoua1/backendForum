package com.web.forumSocialX.poste;


import com.web.forumSocialX.comment.Comment;
import com.web.forumSocialX.comment.CommentRepository;
import com.web.forumSocialX.firebase.FirebaseStorageService;
import com.web.forumSocialX.interaction.Interaction;
import com.web.forumSocialX.interaction.InteractionRepository;
import com.web.forumSocialX.notification.Notification;
import com.web.forumSocialX.notification.NotificationRepository;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class PosteService {
    private final CommentRepository commentRepository;

    private final PosteRepository posteRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notifRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final InteractionRepository interRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
     public Poste addPoste(Poste poste ,User user){
         boolean exist = userRepository.existsById(user.getId());

         if(exist)

         {  Date dateDeCreation = new Date();
             poste.setDateCreate(dateDeCreation);
            poste.setUser(user);
         return  posteRepository.save(poste);

    }
     return null;
     }

    public Optional<Poste> findById(Long id)
    {        return posteRepository.findById(id);
    }

    public List<Poste> findAll()
  {
    return posteRepository.findAll(Sort.by(Sort.Direction.DESC, "dateCreate"));

    }
    public List<Poste> getEnabledPostes() {
        return posteRepository.findByEnabled(true,Sort.by(Sort.Direction.DESC, "dateCreate"));
    }
    @Transactional
    public Poste updatePosteImage(Long id, MultipartFile multipartFile) {

        Poste poste= posteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("poste not found for posteId: " + id));

        List<String> folderNames = Arrays.asList("ImagePoste",poste.getUser().getUsername());
        String  imageUrl= firebaseStorageService.upload(multipartFile,folderNames);
        if(imageUrl != null) {
            poste.setImage(imageUrl);
        }
            // Sauvegarder les modifications dans la base de données
            Poste p = posteRepository.save(poste);
            return p;

    }
    @Transactional
    public Poste updatePoste(Poste poste ,MultipartFile multipartFile ,Boolean deleteImage ,Long id){
        try {
            Poste p =posteRepository.getReferenceById(id);
            p.setMessage(poste.getMessage());
            p.setUser(p.getUser());

            p.setDateCreate(p.getDateCreate());
            p.setCategory(poste.getCategory());

            p.setComments(p.getComments());

            if (multipartFile != null && !multipartFile.isEmpty())
            {
                List<String> folderNames = Arrays.asList("ImagePoste",p.getUser().getUsername());
                String  imageUrl= firebaseStorageService.upload(multipartFile,folderNames);

                p.setImage(imageUrl);
            }else if(deleteImage)
            {p.setImage(null);}
            else{
                p.setImage(p.getImage());
            }
          return  posteRepository.save(p);

        } catch (EntityNotFoundException e) {
            // Log the error and rethrow
            throw e; // Optionally log this error
        } catch (Exception e) {
            // Log the error and handle it appropriately
            // Optionally log the exception
            throw new RuntimeException("Failed to update poste", e);
        }
    }

    public  Poste deletePoste(Long id)

    {
        boolean exist = posteRepository.existsById(id);
        Poste p =posteRepository.getReferenceById(id);
        if(exist)
        {
           posteRepository.delete(p);
            return p;
        }
        return null;
    }

    @Transactional
    public ResponseEntity<Poste> addPostWithImage(Poste poste ,MultipartFile file , Long id){
        try {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            poste.setUser(user);

            if(!file.isEmpty() && file !=null) {
                List<String> folderNames = Arrays.asList("ImagePoste",poste.getUser().getUsername());
                String  imageUrl= firebaseStorageService.upload(file,folderNames);
                poste.setImage(imageUrl);
            }
            else {
                String imageUrl="";
                poste.setImage(imageUrl);
            }
            Date dateDeCreation = new Date();
            poste.setDateCreate(dateDeCreation);
            posteRepository.save(poste);
            return new ResponseEntity<>(poste, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    public int getTotalLikes(Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        return optionalPoste.map(poste -> poste.getInteractions()
                .stream()
                .mapToInt(Interaction::getLike)
                .sum()).orElse(0);
    }
    public int getTotalDislikes(Long postId) {
        Optional<Poste> optionalPoste = posteRepository.findById(postId);
        return optionalPoste.map(poste -> poste.getInteractions()
                .stream()
                .mapToInt(Interaction::getDislike)
                .sum()).orElse(0);
    }


    public Poste getPosteByNotification(Notification notification) {
        return posteRepository.findById(notification.getPoste().getId()).orElse(null);
    }


    @Transactional
   public  void  disablePoste(Long id )
   {
      Poste poste = posteRepository.findById(id).orElseThrow(() -> new RuntimeException("Poste not found"));
      poste.setEnabled(false);
       List<Notification> notifications = notifRepository.findByPoste(poste);
       for (Notification notification : notifications) {
           notification.setEnabled(false);
           notifRepository.save(notification);

       }
       List<Interaction> interactions = interRepository.findByPoste(poste);
       for (Interaction interaction : interactions) {



           List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
           for (Notification interNotification : interNotifications) {
               interNotification.setEnabled(false);
               notifRepository.save(interNotification);
           }



       }
       // Désactiver les commentaires liés au poste
       List<Comment> comments = commentRepository.findByPoste(poste);
       for (Comment comment : comments) {


           // Désactiver les notifications liées aux commentaires
           List<Notification> commentNotifications = notifRepository.findByComment(comment);
           for (Notification commentNotification : commentNotifications) {
               commentNotification.setEnabled(false);
               notifRepository.save(commentNotification); // Enregistrer les notifications de commentaires désactivées
           }
           List<Interaction> interactionsComment = interRepository.findByComment(comment);
           for (Interaction interaction : interactionsComment) {


               // Désactiver les notifications liées aux interactions
               List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
               for (Notification interNotification : interNotifications) {
                   interNotification.setEnabled(false);
                   notifRepository.save(interNotification); // Enregistrer les notifications de commentaires désactivées
               }

           }

       }

  posteRepository.save(poste);
   }

   @Transactional
    public  void  enablePoste(Long id )
    {
        Poste poste = posteRepository.findById(id).orElseThrow(() -> new RuntimeException("Poste not found"));
        poste.setEnabled(true);
        List<Notification> notifications = notifRepository.findByPoste(poste);
        for (Notification notification : notifications) {
            notification.setEnabled(true);
            notifRepository.save(notification);

        }
        List<Interaction> interactions = interRepository.findByPoste(poste);
        for (Interaction interaction : interactions) {



            List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
            for (Notification interNotification : interNotifications) {
                interNotification.setEnabled(true);
                notifRepository.save(interNotification);
            }



        }
            List<Comment> comments = commentRepository.findByPoste(poste);
            for (Comment comment : comments) {

                if(comment.isEnabled())
                {
                    List<Notification> commentNotifications = notifRepository.findByComment(comment);
                    for (Notification commentNotification : commentNotifications) {
                        commentNotification.setEnabled(true);
                        notifRepository.save(commentNotification); // Enregistrer les notifications de commentaires désactivées
                    }
                    List<Interaction> interactionsComment = interRepository.findByComment(comment);
                    for (Interaction interaction : interactionsComment) {


                        // Désactiver les notifications liées aux interactions
                        List<Notification> interNotifications = notifRepository.findByInteraction(interaction);
                        for (Notification interNotification : interNotifications) {
                            interNotification.setEnabled(true);
                            notifRepository.save(interNotification); // Enregistrer les notifications de commentaires désactivées
                        }

                    }
                }
            }


        posteRepository.save(poste);
    }

    public Poste findPosteById(Long id)
    {
       return posteRepository.findById(id).orElseThrow(() -> new RuntimeException("Poste not found"));

    }



    public long getPostCountByUserId(Long userId) {
        return posteRepository.countEnabledPostsByUserId(userId);
    }
}
