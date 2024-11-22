package com.web.forumSocialX.config;


import com.web.forumSocialX.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling

@RequiredArgsConstructor
public class SchedulingConfig {

    private final UserService userService;

    @Scheduled(fixedRate = 60000) // Check every 5 minutes
    public void checkInactiveUsers() {
        userService.checkInactiveUsers();
    }
}
