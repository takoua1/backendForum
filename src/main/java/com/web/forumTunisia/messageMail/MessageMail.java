package com.web.forumTunisia.messageMail;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.web.forumTunisia.comment.Comment;
import com.web.forumTunisia.poste.Poste;
import com.web.forumTunisia.signale.Signale;
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
public class MessageMail {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotNull(message="remplir")
    private String objet;
    @Column(columnDefinition = "TEXT")
    @NotNull(message="remplir")
    private String contenu;
    private String type;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "signale_id")
    private Signale signale;
    private Date dateCreate;
    @Column(nullable = false)
    private Boolean enabled = true;

}
