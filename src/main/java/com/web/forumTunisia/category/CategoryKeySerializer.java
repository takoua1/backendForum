package com.web.forumTunisia.category;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CategoryKeySerializer extends JsonSerializer<Category> {
    @Override
    public void serialize(Category category, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(category.name()); // Ou tout autre format personnalisé
    }
}