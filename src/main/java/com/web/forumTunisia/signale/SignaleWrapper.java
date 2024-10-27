package com.web.forumTunisia.signale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data

@Builder
@AllArgsConstructor
public class SignaleWrapper {
    String id;
    String titre;
    String raison;
    String description;
    String dateSignale;
    String nom;
    String prenom;
}
