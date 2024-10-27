package com.web.forumTunisia.block;

import com.web.forumTunisia.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Block {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blocker_id")
    private User blocker; // Celui qui bloque

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blocked_id")
    private User blocked; // Celui qui est bloqué

    private Date blockedAt;

}
