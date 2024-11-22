package com.web.forumSocialX.messageMail;


import com.web.forumSocialX.signale.Signale;
import com.web.forumSocialX.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Date;

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
    private boolean read;
    @Column(nullable = false)
    private Boolean enabled = true;

}
