package com.web.forumTunisia.signale;


import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignaleRepository   extends JpaRepository <Signale, Long>{
    List<Signale> findByPosteId(Long posteId);
    List<Signale> findByCommentId(Long commentId);
    List<Signale> findByEnabledTrue(Sort sort);
}
