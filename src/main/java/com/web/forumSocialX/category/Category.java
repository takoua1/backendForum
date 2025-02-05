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
    Education,
    Technologie,

    Animaux,          // Nouvelle catégorie pour les animaux
    Voyage,           // Nouvelle catégorie pour les voyages
    Culture,          // Nouvelle catégorie pour la culture
    Science,          // Nouvelle catégorie pour les sciences
    Santé,            // Nouvelle catégorie pour la santé
    Cuisine,          // Nouvelle catégorie pour la cuisine
    Histoire,         // Nouvelle catégorie pour l'histoire
    Art,              // Nouvelle catégorie pour les arts
    Environnement     // Nouvelle catégorie pour l'environnement


}
