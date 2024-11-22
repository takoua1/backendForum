package com.web.forumSocialX.messageMail;


import com.web.forumSocialX.chat.ChatController;
import com.web.forumSocialX.signale.Signale;
import com.web.forumSocialX.signale.SignaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/mail")
@RequiredArgsConstructor
public class MessageMailController {
    private final MessageMailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final SimpMessagingTemplate messagingTemplate;
   private final SignaleService sigService;
    @MessageMapping("/sendMsgAvr")
    public void sendMail(@Payload Signale signale) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String datePub;
        String contenu;
        String action;
        MessageMail msgAvert = new MessageMail();

        // Définir le contenu et l'action en fonction de la source du signalement (Poste ou Commentaire)
        if (signale.getPoste() != null) {
            action = "Supprimer votre post";
            contenu = signale.getPoste().getMessage();
            datePub = formatter.format(signale.getPoste().getDateCreate());
        } else if (signale.getComment() != null) {
            action = "Supprimer votre commentaire";
            contenu = signale.getComment().getText();
            datePub = formatter.format(signale.getComment().getDateCreate());
        } else {
            throw new IllegalArgumentException("Le signalement doit être lié à un poste ou un commentaire.");
        }

        // Configuration du message d'avertissement
        msgAvert.setObjet("Avertissement concernant la publication de votre post/commentaire");
        msgAvert.setContenu("Cher(e) Utilisateur/Utilisatrice,\n" +
                "\n" +
                "Nous espérons que vous profitez pleinement de votre expérience sur notre plateforme. Nous tenons à maintenir un environnement sûr et respectueux pour tous nos utilisateurs, c'est pourquoi nous vous contactons aujourd'hui.\n" +
                "\n" +
                "Nous avons récemment constaté qu'un de vos posts enfreint nos règles de conduite. Notre objectif est de protéger la communauté et de veiller à ce que tous les échanges soient courtois, respectueux et conformes à nos conditions d'utilisation.\n" +
                "\n" +
                "Publication concernée :\n" +
                "Date de publication : " + datePub.toString() + "\n" +
                "Texte du post ou commentaire : " + contenu + "\n" +
                "Raison de l'avertissement : " + signale.getRaison() + "\n" +
                "\n" +
                "Nous vous encourageons à revoir notre charte de la communauté pour mieux comprendre les comportements attendus sur notre plateforme. Si vous avez des questions ou si vous souhaitez contester cet avertissement, n'hésitez pas à nous contacter. Nous sommes là pour vous aider et répondre à toutes vos préoccupations.\n" +
                "\n" +
                "Action requise : " + action + "\n" +
                "\n" +
                "À noter : En cas de récidive ou de non-respect répété de nos règles, nous pourrions être amenés à prendre des mesures plus strictes, pouvant aller jusqu'à la suspension de votre compte.\n" +
                "\n" +
                "Nous vous remercions de votre attention à ce message et vous souhaitons une agréable continuation sur notre plateforme.\n" +
                "\n" +
                "Cordialement,\n" +
                "L'Équipe de ForumSocialX");
        msgAvert.setType("Avert");
        msgAvert.setSignale(signale);
        msgAvert.setDateCreate(new Date());
        msgAvert.setUser(signale.getPoste() != null ? signale.getPoste().getUser() : signale.getComment().getUser());
        // Enregistrer et envoyer le message d'avertissement à l'utilisateur concerné
        mailService.createMessage(msgAvert);
        if (signale.getPoste() != null) {
            messagingTemplate.convertAndSendToUser(
                    signale.getPoste().getUser().getUsername(),
                    "/queue/mail",
                    new MessageRequest(msgAvert.getId().toString(), msgAvert.getObjet(), msgAvert.getContenu())
            );
        } else if (signale.getComment() != null) {
            messagingTemplate.convertAndSendToUser(
                    signale.getComment().getUser().getUsername(),
                    "/queue/mail",
                    new MessageRequest(msgAvert.getId().toString(), msgAvert.getObjet(), msgAvert.getContenu())
            );
        }

    }

    @MessageMapping("/sendMsgInfo")
    public void sendMsgInfo(@Payload Signale signale) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        String datePub="";
        String contenu="";
        // Envoyer le message d'information à tous les utilisateurs ayant signalé
        List<Signale> signales = sigService.listerSignale(signale);
        MessageMail msgInfor = new MessageMail();
        System.out.println("Nombre de signales retournés : " + signales.size());
        for (Signale s : signales) {


            if (s.getPoste() != null) {
                contenu = s.getPoste().getMessage();
                datePub = formatter.format(s.getPoste().getDateCreate());
            } else if (s.getComment() != null) {
                contenu = s.getComment().getText();
                datePub = formatter.format(s.getComment().getDateCreate());
            }

            msgInfor.setObjet("Confirmation de votre signalement");
            msgInfor.setContenu("Cher(e) Utilisateur/Utilisatrice,\n" +
                    "\n" +
                    "Nous vous remercions d'avoir pris le temps de signaler un contenu sur notre plateforme. Votre vigilance contribue à maintenir notre communauté sûre et respectueuse pour tous ses membres.\n" +
                    "\n" +
                    "Nous avons bien reçu votre signalement concernant le post suivant :\n" +
                    "Date de publication : " + datePub.toString() + "\n" +
                    "Texte du post ou commentaire : " + contenu + "\n" +
                    "Raison du signalement : " + s.getRaison() + "\n" +
                    "\n" +
                    "Soyez assuré(e) que notre équipe de modération prend très au sérieux chaque signalement. Nous examinerons le post en question dans les plus brefs délais et prendrons les mesures appropriées en fonction de nos règles de conduite et de nos conditions d'utilisation.\n" +
                    "\n" +
                    "Votre contribution est essentielle pour nous aider à maintenir un espace sain et accueillant pour tous les utilisateurs. Si vous avez d'autres préoccupations ou questions, n'hésitez pas à nous contacter.\n" +
                    "\n" +
                    "Encore une fois, merci pour votre vigilance et votre engagement envers notre communauté.\n" +
                    "\n" +
                    "Cordialement,\n" +
                    "L'Équipe de ForumSocialX");
            msgInfor.setType("Info");
            msgInfor.setSignale(s);
            msgInfor.setDateCreate(new Date());
            msgInfor.setUser(s.getUser());
            // Enregistrer et envoyer le message d'information
            mailService.createMessage(msgInfor);
            messagingTemplate.convertAndSendToUser(
                    s.getUser().getUsername(),
                    "/queue/mail",
                    new MessageRequest(msgInfor.getId().toString(), msgInfor.getObjet(), msgInfor.getContenu())
            );
        }
    }

    @MessageMapping("/sendMsgModif")
    public void sendMsgModif(@Payload Signale signale) throws Exception {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String datePub;
        String contenu;
        String action;
        MessageMail msgModif = new MessageMail();

        // Définir le contenu et l'action en fonction de la source du signalement (Poste ou Commentaire)
        if (signale.getPoste() != null) {
            action = "Supprimer votre post";
            contenu = signale.getPoste().getMessage();
            datePub = formatter.format(signale.getPoste().getDateCreate());
        } else if (signale.getComment() != null) {
            action = "Supprimer votre commentaire";
            contenu = signale.getComment().getText();
            datePub = formatter.format(signale.getComment().getDateCreate());
        } else {
            throw new IllegalArgumentException("Le signalement doit être lié à un poste ou un commentaire.");
        }

            msgModif.setObjet("Mise à jour concernant la décision prise sur votre post/commentaire signalé");
            msgModif.setContenu("Cher(e) Utilisateur/Utilisatrice,\n" +
                    "\n" +
                    "Nous tenons à vous informer qu'après une réévaluation de votre signalement concernant le post/commentaire suivant, nous avons décidé de revenir sur notre décision initiale de suppression :\n" +
                    "Date de publication : " + datePub.toString() + "\n" +
                    "Texte du post ou commentaire : " + contenu + "\n" +
                    "Raison initiale du signalement : " + signale.getRaison() + "\n" +
                    "\n" +
                    "Nouvelle décision : Le contenu sera rétabli.\n" +
                    "Motif de la révision : Après un examen plus approfondi, nous avons estimé que le contenu ne contrevient pas à nos règles de conduite et mérite d'être rétabli sur la plateforme.\n" +
                    "\n" +
                    "Nous comprenons que ces décisions peuvent être complexes, et nous vous assurons que chaque signalement est traité avec la plus grande rigueur. Votre vigilance et votre engagement envers notre communauté sont précieux, et nous vous remercions pour cela.\n" +
                    "\n" +
                    "Si vous avez des questions ou des préoccupations concernant cette révision, n'hésitez pas à nous contacter. Nous sommes à votre disposition pour toute clarification ou discussion complémentaire.\n" +
                    "\n" +
                    "Encore une fois, merci pour votre contribution à la qualité de notre communauté.\n" +
                    "\n" +
                    "Cordialement,\n" +
                    "L'Équipe de ForumSocialX");

        msgModif.setSignale(signale);
        msgModif.setDateCreate(new Date());
        msgModif.setUser(signale.getPoste() != null ? signale.getPoste().getUser() : signale.getComment().getUser());
        // Enregistrer et envoyer le message d'avertissement à l'utilisateur concerné
        mailService.createMessage(msgModif);
        if (signale.getPoste() != null) {
            messagingTemplate.convertAndSendToUser(
                    signale.getPoste().getUser().getUsername(),
                    "/queue/mail",
                    new MessageRequest(msgModif.getId().toString(), msgModif.getObjet(), msgModif.getContenu())
            );
        } else if (signale.getComment() != null) {
            messagingTemplate.convertAndSendToUser(
                    signale.getComment().getUser().getUsername(),
                    "/queue/mail",
                    new MessageRequest(msgModif.getId().toString(), msgModif.getObjet(), msgModif.getContenu())
            );
        }



    }



    @GetMapping("/user/{username}")
    public List<MessageMail> getSignalementsPaged(@PathVariable String username) {


        return mailService.findMessagesByUser(username);
    }

    @PatchMapping("/disable/{id}")
    public ResponseEntity<MessageMail> disableSignale(@PathVariable Long id) {
        MessageMail updatedMail = mailService.disableMail(id);
        return ResponseEntity.ok(updatedMail);
    }

    @PatchMapping("/markAsRead/{messageId}")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {

        try {

            mailService.markMessageAsRead(messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }}
}

