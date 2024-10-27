package com.web.forumTunisia.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor

    public class TypingEvent {
        private String senderId;
        private String recipientId;
        private boolean isTyping;

        // getters and setters
    }


