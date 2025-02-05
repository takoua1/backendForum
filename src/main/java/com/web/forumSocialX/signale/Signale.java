package com.web.forumSocialX.signale;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.web.forumSocialX.comment.Comment;
import com.web.forumSocialX.messageMail.MessageMail;
import com.web.forumSocialX.poste.Poste;
import com.web.forumSocialX.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Signale {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titre;
    private String raison;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Date dateSignale;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "poste_id")
    private Poste poste;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    private boolean estTraite;


    @Enumerated(EnumType.STRING)

    private Decision decision ;

    private Date dateDecision;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;
    @Column(nullable = false)
    private Boolean enabled = true;
    @JsonIgnore
    @OneToMany(mappedBy = "signale", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<MessageMail> messageMails;

}
