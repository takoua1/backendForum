package com.web.forumTunisia.groupe;

import com.web.forumTunisia.category.Category;
import com.web.forumTunisia.chat.Chat;
import com.web.forumTunisia.poste.Poste;
import com.web.forumTunisia.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
@EnableJpaRepositories
public interface GroupeRepository  extends JpaRepository<Groupe,Long> {
    List<Groupe> findByChat_Membres_Id(Long userId);
    @Query("SELECT g FROM Groupe g JOIN g.chat c JOIN c.membres m WHERE m.id = :userId")
    List<Groupe> findByMembresId(@Param("userId") Long userId, Sort sort);
    List<Groupe> findByDateCreateBetween(Date startDate, Date endDate);
    List<Groupe> findByUserCreature(User user);
    List<Groupe> findByCategoryAndDateCreateBetween(Category category, Date startDate, Date endDate);
}
