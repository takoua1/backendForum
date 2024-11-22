package com.web.forumSocialX.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data

@Builder
@AllArgsConstructor
public class NotificationWrapper {

    String id;
    String actor;
    String imageActor;
    String message;
    String reaction;
    String posteId;
    String commentId;
    String interactionId;
    String recipent ;
    String dateCreate;


}
