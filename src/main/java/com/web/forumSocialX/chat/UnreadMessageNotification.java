package com.web.forumSocialX.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class UnreadMessageNotification {

    private Long chatId;
    private int unreadCount;
}
