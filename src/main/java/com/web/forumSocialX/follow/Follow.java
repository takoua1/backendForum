package com.web.forumSocialX.follow;

import com.web.forumSocialX.user.User;
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
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    private User follower;  // Celui qui suit

    @ManyToOne
    @JoinColumn(name = "followed_id")
    private User followed; // Celui qui est suivi

    private Date followedAt;
}
