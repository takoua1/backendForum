package com.web.forumSocialX.user;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ChangePasswordRequest {

    private String currentPassword;
    private String newPassword;
    private String confirmationPassword;
}
