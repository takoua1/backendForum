package com.web.forumSocialX.user;


import com.fasterxml.jackson.annotation.*;
import com.web.forumSocialX.chat.Chat;
import com.web.forumSocialX.comment.Comment;
import com.web.forumSocialX.follow.Follow;
import com.web.forumSocialX.groupe.Groupe;
import com.web.forumSocialX.interaction.Interaction;
import com.web.forumSocialX.message.Message;
import com.web.forumSocialX.poste.Poste;

import com.web.forumSocialX.token.Token;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;



import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
@Table(name="users",schema="public")

@JsonIgnoreProperties({"authorities", "accountNonLocked", "credentialsNonExpired", "hibernateLazyInitializer", "handler"})


public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;

    @Column(unique = true,nullable = false)
    @NotNull(message="remplir" )
    private String username;
    @NotNull(message="remplir")
    private String nom;
    @NotNull(message="remplir")
    private String prenom;
    private String education;
    private String adresse;
    private String emploi;
    private String compteFacebook;
    private String compteInstagram;
    private String compteLinked;
    private String compteTwitter;
    @Column(columnDefinition = "TEXT")
    private String bio;
    @NotNull(message="remplir")
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    private String pays;
     private String tel;
    @Enumerated(EnumType.STRING)
    private Status status;
    private boolean emailVerified;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastActive;
    private String image;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm", timezone = "UTC")
   private Date  dateAuthenticated;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Poste> postes;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interaction> interactions=new ArrayList<>();
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "userCreature", fetch = FetchType.EAGER, cascade = CascadeType.ALL)


    private List<Groupe> groupes = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false)
    private boolean enabled = true;
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Message> sentMessages = new ArrayList<>();

    @ManyToMany(mappedBy = "receivers" , cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Message> receivedMessages;

    @JsonIgnore


    @ManyToMany(mappedBy = "membres", cascade = {CascadeType.PERSIST, CascadeType.MERGE})

   private List<Chat> chats = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "follower" , cascade = CascadeType.ALL ,orphanRemoval = true)
    private List<Follow> following;
    @JsonIgnore
    @OneToMany(mappedBy = "followed" ,cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Follow> followers;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Token> tokens;



    @Override

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override

    public String getPassword() {
        return password;
    }

    @Override

    public String getUsername() {
        return username;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override

    public boolean isAccountNonLocked() {
        return true;
    }

    @Override

    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


}
