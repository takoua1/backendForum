package com.web.forumTunisia.messageMail;


import com.web.forumTunisia.signale.Signale;
import com.web.forumTunisia.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageMailRepository extends JpaRepository<MessageMail,Long> {

    List<MessageMail> findByUserAndEnabledTrue(User user, Sort sort);
}
