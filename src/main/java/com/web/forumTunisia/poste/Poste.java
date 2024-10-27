package com.web.forumTunisia.poste;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.web.forumTunisia.category.Category;
import com.web.forumTunisia.comment.Comment;
import com.web.forumTunisia.interaction.Interaction;
import com.web.forumTunisia.notification.Notification;
import com.web.forumTunisia.user.User;
import  jakarta.persistence.*;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(columnDefinition = "TEXT")
    @NotNull(message="remplir")
    private String message;
    @Enumerated(EnumType.STRING)
    @NotNull(message="remplir")
    private Category category;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull(message="remplir")
    private Date dateCreate;
    private String image;
    @Column(nullable = false)
    private boolean enabled = true;
    @OneToMany(mappedBy = "poste", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<Comment> comments = new ArrayList<>();
    @JsonBackReference("poste_interactions")
    @OneToMany(mappedBy = "poste", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interaction> interactions=new ArrayList<>();
    @JsonBackReference("poste_notifications")
    @OneToMany(mappedBy = "poste", cascade = CascadeType.ALL,  orphanRemoval = true)
    private List<Notification> notifications=new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id_poste")
    public User user;
    @Override
    public String toString() {
        return "Poste{" +
                "id=" + id +
                ", message='" + message + '\'' +
                '}';
    }

}
