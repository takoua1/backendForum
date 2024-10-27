package com.web.forumTunisia.interaction;

import com.web.forumTunisia.comment.Comment;
import com.web.forumTunisia.poste.Poste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction,Long> {
    List<Interaction> findByUserIdAndPosteIdAndType(Long userId, Long posteId, String type);
    List<Interaction> findByUserIdAndCommentIdAndType(Long userId, Long commentId, String type);
    @Query("SELECT p.id, SUM(i.like) FROM Poste p JOIN p.interactions i GROUP BY p.id")
    List<Object[]> countTotalLikesByPoste();

    List<Interaction> findByPoste(Poste poste);
    List<Interaction> findByComment(Comment comment);
}
