package com.web.forumSocialX.messageMail;


import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageMailService {
    private final UserRepository userRepository;

    private final  MessageMailRepository mailRepository;
    public MessageMail createMessage(MessageMail messageMail) {
        messageMail.setDateCreate( new Date());
        return mailRepository.save(messageMail);
    }



    public List<MessageMail> findMessagesByUser(String username) {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        return mailRepository.findByUserAndEnabledTrue(user, Sort.by(Sort.Direction.DESC, "dateCreate"));
    }


    public MessageMail disableMail(Long id)
    {
        MessageMail mail = mailRepository.findById(id).orElseThrow(() -> new RuntimeException("Mail not found"));

        mail.setEnabled(false);
        return mailRepository.save(mail);
    }
    public void markMessageAsRead(Long messageId) {
        // Marquer un message spécifique comme lu
        Optional<MessageMail> mailOptional = mailRepository.findById(messageId);
        if (mailOptional.isPresent()) {
            MessageMail message = mailOptional.get();
            message.setRead(true);
            mailRepository.save(message);
        } else {
            // Gérer l'erreur si le message n'est pas trouvé
            throw new IllegalArgumentException("Message not found with ID: " + messageId);
        }
    }
}
