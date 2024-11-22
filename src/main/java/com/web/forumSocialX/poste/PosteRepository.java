package com.web.forumSocialX.poste;


import com.web.forumSocialX.category.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
public interface PosteRepository extends JpaRepository<Poste, Long> {

    Optional<Poste> findById(Long id);
    @Query("SELECT COUNT(p) FROM Poste p WHERE p.user.id = :userId AND p.enabled = true")
    long countEnabledPostsByUserId(@Param("userId") Long userId);
    List<Poste> findByCategory(Category category);
  /*  @Query("SELECT p.category, COUNT(p) FROM Poste p GROUP BY p.category")
    List<Object[]> countPostesByCategory();*/

    @Query("SELECT p.category, COUNT(p) FROM Poste p WHERE p.dateCreate >= :startDate GROUP BY p.category")
    Map<Category, Long> countPostesByCategoryAndPeriod(@Param("startDate") Date startDate);

    List<Poste> findByDateCreateAfterAndEnabledTrue(Date startDate);

    List<Poste> findByEnabled(boolean enabled , Sort sort);
    List<Poste> findByCategoryAndDateCreateBetweenAndEnabledTrue(Category category, Date startDate, Date endDate);
    List<Poste> findByDateCreateBetweenAndEnabledTrue(Date startDate, Date endDate);
}
