package com.web.forumSocialX.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class GroupNotification {
    private String id;
    private String groupId;
    private String senderId;
    private String senderUsername;

    private String senderNom;
    private String senderPrenom;
    private String senderEmail;
    private  String imageProfile;
    private String image;
    private String video;
    private String audio;
    private String content;

    private String times;
}
