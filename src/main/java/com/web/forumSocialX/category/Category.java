package com.web.forumSocialX.category;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CategoryKeySerializer.class)
public enum Category {

 Jeux,
 Sport,
    Politique,
    Musique,
    Education


}
