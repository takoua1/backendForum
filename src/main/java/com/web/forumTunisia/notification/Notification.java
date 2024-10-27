package com.web.forumTunisia.notification;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.web.forumTunisia.comment.Comment;
import com.web.forumTunisia.interaction.Interaction;
import com.web.forumTunisia.poste.Poste;
import com.web.forumTunisia.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;
    private String message;
    @JsonBackReference("user")
    @ManyToMany
    @JoinTable(
            name = "noitifs_users",
            joinColumns = @JoinColumn(name = "notif_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> recipients= new ArrayList<>();
    @Column(nullable = false)
    private boolean enabled = true;
  //  @JsonManagedReference("poste_notification")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poste_id")
    private Poste poste;

   // @JsonManagedReference("comment_notification")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interaction_id")
    private Interaction interaction;
    private String reaction;

    private Date dateCreate;
    private boolean read;

}
