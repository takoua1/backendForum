package com.web.forumTunisia.message;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.forumTunisia.chat.Chat;
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
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String image;
    private String video;
    private String audio;

    private Date time;

    @ManyToOne
    @JoinColumn(name = "sender_id" )

    private User sender;

    @ManyToMany
    @JoinTable(
            name = "message_users",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonBackReference("user")
    private List<User> receivers;
    private boolean read;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chat_id")

    private Chat chat;
    public boolean getRead() {
        return read;
    }
}

