package com.web.forumTunisia.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.Collections;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.web.forumTunisia.user.Permission.ADMIN_CREATE;
import static  com.web.forumTunisia.user.Permission.ADMIN_DELETE;
import static  com.web.forumTunisia.user.Permission.ADMIN_READ;
import static  com.web.forumTunisia.user.Permission.ADMIN_UPDATE;
@RequiredArgsConstructor
public enum Role {

    USER ,
    ADMIN
}
