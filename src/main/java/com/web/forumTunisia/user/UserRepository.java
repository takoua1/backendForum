package com.web.forumTunisia.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User>findById(Long id);
    List<User> findByLastActiveBefore(LocalDateTime threshold);
    List<User>  findByDateAuthenticatedBetweenAndEnabledTrueAndRole(Date startDate, Date endDate,Role role);

    long countByStatus(Status status);

    @Query("SELECT u FROM User u WHERE u.role <> 'USER'")
    List<User> findAllExceptUsers();
    List<User> findAllByRole(Role role,Sort sort);


}
