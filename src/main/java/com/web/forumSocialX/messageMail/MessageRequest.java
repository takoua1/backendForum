package com.web.forumSocialX.messageMail;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class MessageRequest {
    String id;

    String objet;
    String contenu;

}
