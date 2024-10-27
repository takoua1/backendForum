package com.web.forumTunisia.message;

import com.web.forumTunisia.chat.Chat;
import com.web.forumTunisia.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@EnableJpaRepositories
public interface MessageRepository extends JpaRepository<Message,Long> {
  /*  @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :senderId AND :receiversId MEMBER OF m.receivers) " +
            "OR (m.sender.id = :receiversId AND :senderId MEMBER OF m.receivers)")
    List<Message> findMessagesBetweenUsers(@Param("senderId") Long senderId, @Param("receiversId") Long receiversId);*/
    List<Message> findBySenderId(Long senderId);

  @Query("SELECT m.chat FROM Message m WHERE m.sender = :sender1 OR m.sender = :sender2 GROUP BY m.chat HAVING COUNT(DISTINCT m.sender) = 2")
  List<Chat> findCommonChats(@Param("sender1") User sender1, @Param("sender2") User sender2);
  List<Message> findByChatId(Long chatId);
  List<Message> findByIdAndReadFalse(Long id);
  @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.sender != :user AND :user MEMBER OF m.receivers AND m.read = false")
  Long countUnreadMessagesByChatAndUser(@Param("chatId") Long chatId, @Param("user") User user);

  @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId  AND m.sender != :user AND :user MEMBER OF m.receivers AND m.read = false")
  List<Message> findUnreadMessagesByChatAndUser(@Param("chatId") Long chatId, @Param("user") User user);
  List<Message> findBySender(User sender);
  @Query("SELECT m FROM Message m JOIN m.receivers r WHERE r.id = :userId")
  List<Message> findByReceiversId(@Param("userId") Long userId);
  void deleteBySenderId(Long senderId);

  Message findTopByOrderByIdDesc();
}
