package com.web.forumSocialX.interaction;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Slf4j
@RequestMapping(path = "/interaction")
@RequiredArgsConstructor
public class InteractionController {
    private final InteractionService interactionService;






    @PostMapping("/poste")
    public Interaction posteInteraction(@RequestBody Interaction interaction)
    {
       return interactionService.interaction(interaction);

    }
    @PostMapping("/comment")
    public Interaction commentInteraction(@RequestBody Interaction interaction)
    {
        return interactionService.interaction(interaction);

    }
    @GetMapping("/find/poste/{userId}/{posteId}/{type}")
    public Interaction getInteractionByUserIdAndPosteId(@PathVariable Long userId, @PathVariable Long posteId, @PathVariable String type) {
        Optional<Interaction> interaction = interactionService.findInteractionByUserIdAndPosteIdType(userId, posteId,type);
        if (interaction.isPresent()) {
            return interaction.get();
        } else {
            return null;
        }
    }
    @GetMapping("/find/comment/{userId}/{commentId}/{type}")
    public Interaction getInteractionByUserIdAndCommentId(@PathVariable Long userId, @PathVariable Long commentId, @PathVariable String type) {
        Optional<Interaction> interaction = interactionService.findInteractionByUserIdAndCommentIdType(userId, commentId,type);
        if (interaction.isPresent()) {
            return interaction.get();
        } else {
            return null;
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id){

        Interaction inter = interactionService.remove(id);
        if(inter!=null)
        {
            return   ResponseEntity.ok("Interaction supprimer avec succès");
        }
        return null;
    }



}
