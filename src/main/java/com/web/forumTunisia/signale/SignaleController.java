package com.web.forumTunisia.signale;



import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/signale")
@RequiredArgsConstructor
public class SignaleController {
  private final SignaleService signaleService;
    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/signale")
    public void Signale(@Payload Signale signale) throws Exception {

        Signale sig= signaleService.Signaler(signale);
        System.out.println("Signale saved: " + sig);
   messagingTemplate.convertAndSend("/topic/signale", new SignaleWrapper(sig.getId().toString(),sig.getTitre(),sig.getRaison(),sig.getDescription(),sig.getDateSignale().toString(),sig.getUser().getNom(),sig.getUser().getPrenom()));


    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Signale> getSignalementsPaged() {
        return signaleService.findAllEnabledSignales();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/add-decision/{signaleId}/{adminUsername}")
    public ResponseEntity<List<Signale>> addDecision(
            @PathVariable Long signaleId,

            @PathVariable String adminUsername,
            @RequestBody Decision newDecision

    ) {
        return signaleService.addDecision(signaleId, newDecision, adminUsername);
    }

    @PreAuthorize("hasRole('ADMIN')")
    // Mettre à jour une décision
    @PatchMapping("/update-decision/{signaleId}/{adminUsername}")
    public ResponseEntity<List<Signale>> updateDecision(@PathVariable Long signaleId, @PathVariable String adminUsername,@RequestBody Decision newDecisions) {
        return signaleService.updateDecision(signaleId, newDecisions,adminUsername);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user-posts-comments/{userId}")
    public ResponseEntity<List<Signale>> getSignalesByPosteOrCommentUser(@PathVariable Long userId) {
        List<Signale> signales = signaleService.getSignalesByPosteOrCommentUser(userId);
        return new ResponseEntity<>(signales, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/disable/{id}")
    public ResponseEntity<Signale> disableSignale(@PathVariable Long id) {
        Signale updatedSignale = signaleService.disableSignale(id);
        return ResponseEntity.ok(updatedSignale);
    }
}
