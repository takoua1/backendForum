package com.web.forumSocialX.auth;


import com.web.forumSocialX.user.Role;
import com.web.forumSocialX.user.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    private String username;
    private String nom;
    private String prenom;
    private String password;
    private String email;

    private Status status;

    private int tel;
    private Role role;
}
