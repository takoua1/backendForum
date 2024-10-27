package com.web.forumTunisia.messageMail;


import com.web.forumTunisia.signale.Signale;
import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
}
