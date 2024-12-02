package com.web.forumSocialX.chat;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.web.forumSocialX.groupe.Groupe;
import com.web.forumSocialX.message.Message;
import com.web.forumSocialX.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor

public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private TypeChat typeChat;


    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "chat_users",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> membres = new ArrayList<>();

 private Date lastMessage;
    //@JsonBackReference("message")
    @JsonIgnore
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<Message> messages = new ArrayList<>();
    @JsonIgnore
    @OneToOne(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private Groupe groupe;

}
