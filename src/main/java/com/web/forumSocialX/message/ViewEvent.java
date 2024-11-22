package com.web.forumSocialX.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor


public class ViewEvent {
    private String username;
    private Long messageId;
    private boolean viewed;
}
