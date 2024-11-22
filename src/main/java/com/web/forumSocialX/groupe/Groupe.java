package com.web.forumSocialX.groupe;


import com.web.forumSocialX.chat.Chat;
import com.web.forumSocialX.category.Category;
import com.web.forumSocialX.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
   private String groupName;
    private String groupImage;
    @Enumerated(EnumType.STRING)

    private Category category;
    private Date dateCreate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userCreature;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ElementCollection
    @MapKeyColumn(name = "user_id")
    @CollectionTable(name = "user_groupe_dates", joinColumns = @JoinColumn(name = "groupe_id"))
    @Column(name = "date_joined")
    private Map<Long, Date> userJoinDates = new HashMap<>();
    @ManyToMany
    @JoinTable(
            name = "groupe_blocked_users",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> blockedMembers = new ArrayList<>();
}


