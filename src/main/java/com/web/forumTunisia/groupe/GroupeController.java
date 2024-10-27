package com.web.forumTunisia.groupe;


import com.web.forumTunisia.category.Category;
import com.web.forumTunisia.user.User;
import com.web.forumTunisia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/groupe")
@RequiredArgsConstructor
public class GroupeController {

    private final GroupeService groupeService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @PostMapping("/addGroupe/{id}")
    public ResponseEntity<Object> addGroupe(@RequestParam("nom") String nom,
                                            @RequestParam("category") Category category,

                                            @RequestParam(value = "file", required = false) MultipartFile file,
                                            @PathVariable Long id) {
        Groupe groupe = new Groupe();
        groupe.setGroupName(nom);
        groupe.setCategory(category);
        return ResponseEntity.ok(groupeService.addGroupe(groupe, file, id));
    }

    @PutMapping("/addMember/{groupeId}/{userId}")
    public ResponseEntity<Object> addMember(@PathVariable Long groupeId, @PathVariable Long userId) {
        try {
            return ResponseEntity.ok(groupeService.addMembre(groupeId, userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/my-groups/{userId}")
    public List<Groupe> getMyGroups(@PathVariable Long userId) {
        return groupeService.findByUser(userId);
    }

    @GetMapping("/findAll")
    public List<Groupe> findAll() {
        return groupeService.findAllGroupes();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        groupeService.deleteGroup(id);
        messagingTemplate.convertAndSend("/topic/group-deleted", id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/quitter/{groupeId}/{userId}")
    public ResponseEntity<Void> quitterGroupe(@PathVariable Long groupeId, @PathVariable Long userId) {
        try {
            groupeService.quitterGroupe(groupeId, userId);
            messagingTemplate.convertAndSend("/topic/group-exit", groupeId);
            return ResponseEntity.ok().build(); // Renvoie un statut 200 OK sans corps
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build(); // Renvoie un statut 400 Bad Request sans corps
        }
    }
    @PatchMapping("/{groupeId}/block/{username}")
    public ResponseEntity<Void> blockMember(@PathVariable Long groupeId, @PathVariable String username) {
        try {
            groupeService.blockMember(groupeId, username);

            messagingTemplate.convertAndSend("/topic/group-block", groupeId);
            // Notifier spécifiquement l'utilisateur bloqué

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{groupeId}/unblock/{username}")
    public ResponseEntity<Void> unblockMember(@PathVariable Long groupeId, @PathVariable String username) {
        try {
            groupeService.unblockMember(groupeId, username);
            messagingTemplate.convertAndSend("/topic/group-unblock", groupeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{groupeId}/deleteMember/{username}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long groupeId, @PathVariable String username) {
        try {
            groupeService.deleteMember(groupeId, username);
            messagingTemplate.convertAndSend("/topic/group-member-delete", groupeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PatchMapping("/{groupeId}/getAdmin/{username}")
    public ResponseEntity<Void> getAdmin(@PathVariable Long groupeId, @PathVariable String username) {
        try {
            groupeService.getAdmin(groupeId, username);
            messagingTemplate.convertAndSend("/topic/group-getAdmin", groupeId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/update/{groupeId}")
    public ResponseEntity<Groupe> updateGroupe(
            @PathVariable Long groupeId,
            @RequestParam("name") String name,
            @RequestParam("category") Category category,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        try {
            Groupe newGroupe=new Groupe();
            newGroupe.setGroupName(name);
            newGroupe.setCategory(category);
            Groupe updatedGroupe = groupeService.updateGroupe(groupeId, file, newGroupe);
            messagingTemplate.convertAndSend("/topic/group-update", groupeId);
            return ResponseEntity.ok(updatedGroupe);
            // Retourne le groupe mis à jour avec un statut 200 OK
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Retourne une réponse 404 si le groupe n'est pas trouvé
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Retourne une réponse 500 en cas d'erreur serveur
        }
    }
}
