package com.web.forumTunisia.comment;


import com.web.forumTunisia.poste.Poste;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CommentRepository  extends JpaRepository<Comment, Long> {


    @Query("SELECT c.parentComment FROM Comment c WHERE c.id = :commentId")
    Comment findParentCommentByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT c.poste FROM Comment c WHERE c.id = :commentId")
    Poste findPosteByCommentId(@Param("commentId") Long commentId);

    List<Comment> findByDateCreateBetweenAndEnabledTrue(Date startDate, Date endDate);

    List<Comment> findByEnabled(boolean enabled, Sort sort);
    List<Comment> findByPoste(Poste poste);
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentId AND c.enabled = true")
    List<Comment> findEnabledChildCommentsByParentId(@Param("parentId") Long parentId);
    Optional<Comment> findById(Long id);
}
