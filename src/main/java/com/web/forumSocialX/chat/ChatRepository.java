package com.web.forumSocialX.chat;


import com.web.forumSocialX.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {
    @Query("SELECT c FROM Chat c WHERE :member MEMBER OF c.membres AND c.typeChat = 'privée'")
    List<Chat> findByMembre(@Param("member") User member, Sort lasteMessage);
    @Query("SELECT c FROM Chat c JOIN c.membres m1 JOIN c.membres m2 WHERE m1 = :member1 AND m2 = :member2")
    List<Chat> findCommonChatsByMembers(@Param("member1") User member1, @Param("member2") User member2);
    @Query("SELECT c FROM Chat c JOIN c.membres m WHERE m.id = :userId")
    List<Chat> findByMembresId(Long userId);

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.id = :chatId")
    void deleteById(Long chatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id = :chatId")
    void deleteMessagesByChatId(Long chatId);
    List<Chat> findChatsByMembresId(Long userId);
}
