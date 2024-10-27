package com.web.forumTunisia.interaction;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.web.forumTunisia.comment.Comment;
import com.web.forumTunisia.notification.Notification;
import com.web.forumTunisia.poste.Poste;
import com.web.forumTunisia.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@Table(name = "interaction")
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "`like`")
    private int like;

    @Column(name = "`dislike`")
    private int dislike;
    private Date datecreation;
    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "poste_id")
    private Poste poste;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    @JsonBackReference("interaction_notifications")
    @OneToMany(mappedBy = "interaction", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Notification> notifications=new ArrayList<>();
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
}
