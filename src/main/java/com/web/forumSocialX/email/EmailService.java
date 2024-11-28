package com.web.forumSocialX.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class EmailService {



    private final JavaMailSender emailSender;



    public void sendVerificationEmail(String to, String name, String firstname, String token) {
        String subject = "Confirmez votre E-Mail - Inscription à l'application ForumSocialX";
        String confirmationUrl = "https://backendforum-1.onrender.com/auth/verify?token=" + token;
   String message = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: auto; background: #fff; border-radius: 5px; padding: 20px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);'>" +
                "<h2 style='color: #2c3e50;'>Cher " + name + " " + firstname + ",</h2>" +
                "<p style='line-height: 1.6;'>Nous sommes ravis de vous accueillir.</p>" +
                "<p style='line-height: 1.6;'>Veuillez cliquer sur le lien ci-dessous pour confirmer votre compte :</p>" +
                "<a href='" + confirmationUrl + "' style='background-color: #3498db; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>Confirmer mon compte</a>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #777;'>" +
                "<p>Cordialement,<br/>" +
                "Équipe d'inscription ForumSocialX</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";


        try {
            // Créez un nouveau message MIME
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            // Créez un helper pour le message
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to); // Définir le destinataire
            helper.setSubject(subject); // Définir le sujet
            helper.setText(message, true); // Définir le contenu en HTML

            // Envoyer le message
            emailSender.send(mimeMessage);
        } catch (MessagingException e) { // Gérer l'exception ici
            e.printStackTrace();
        }
    }
    public void sendPasswordResetEmail(String to, String name, String firstname, String username, String resetUrl) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Réinitialisation du mot de passe");

        String htmlMessage = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: auto; background: #fff; border-radius: 5px; padding: 20px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);'>" +
                "<h2 style='color: #2c3e50;'>Cher " + name + " " + firstname + " (" + username + "),</h2>" +  // Ajout du nom d'utilisateur ici
                "<p style='line-height: 1.6;'>Nous avons reçu une demande de réinitialisation de mot de passe pour votre compte.</p>" +
                "<p style='line-height: 1.6;'>Pour réinitialiser votre mot de passe, cliquez sur le lien ci-dessous :</p>" +
                "<a href=\"" + resetUrl + "\" style='display: inline-block; background-color: #3498db; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>Réinitialiser le mot de passe</a>" +
                "<p style='line-height: 1.6;'>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet e-mail.</p>" +
                "<div style='margin-top: 20px; font-size: 12px; color: #777;'>" +
                "<p>Cordialement,<br/>" +
                "L'équipe de support de ForumSocialX</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlMessage, true);

        emailSender.send(message);
    }


    private String generateConfirmationLink(String token){
        return "<a href=https://backendforum-1.onrender.com/auth/verify?token="+token+">Confirm Email</a>";
    }
}
