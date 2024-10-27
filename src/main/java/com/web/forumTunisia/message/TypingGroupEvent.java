package com.web.forumTunisia.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class TypingGroupEvent {
    private String username;
    private String groupId;
    private boolean typing;


}
