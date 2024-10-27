package com.web.forumTunisia.category;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
@JsonSerialize(using = CategoryKeySerializer.class)
public enum Category {

 Jeux,
 Sport,
    Politique,
    Musique,
    Education


}
