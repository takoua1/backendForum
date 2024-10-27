package com.web.forumTunisia.poste;


import com.web.forumTunisia.category.Category;
import com.web.forumTunisia.firebase.FirebaseStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@Slf4j
@RequestMapping(path = "/poste")
@RequiredArgsConstructor
public class PosteController {

    private final PosteService posteService;
    private final FirebaseStorageService firebaseStorageService;

    @PostMapping("/add")

    public Poste add(@RequestBody Poste poste) {
        return posteService.addPoste(poste, poste.getUser());
    }


    @GetMapping("/findById/{id}")

    public ResponseEntity<Poste> findById(@PathVariable(value = "id") Long id) {
        log.info("Searching for user with username: {}", id);

        return posteService.findById(id)

                .map(poste -> ResponseEntity.ok().body(poste))
                .orElse(ResponseEntity.notFound().build());
    }

        @PutMapping("/updatePoste/{id}")
        public ResponseEntity<Poste> updatePoste(@RequestParam("message") String message,
                                                  @RequestParam("category") Category category,
                                             @RequestParam(value="file",required = false) MultipartFile file,
                                                  @RequestParam("deleteImage") Boolean deleteImage,
                                                  @PathVariable Long id)
        {

            Poste poste = new Poste();

            poste.setMessage(message);
            poste.setCategory(category);

           Poste updatedPoste= posteService.updatePoste(poste, file, deleteImage, id);

                if (updatedPoste != null) {
                    return ResponseEntity.ok(updatedPoste);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 if Poste not found
                }}
  @DeleteMapping("/delete/{id}")
  public ResponseEntity<Object> deletePoste( @PathVariable Long id)
  {
      Poste p = posteService.deletePoste(id);
      if(p!=null)
      {
       return   ResponseEntity.ok("Poste supprimer avec succès");
      }
      return null;
  }
    @PostMapping("/addPostWithImage/{id}")
    public ResponseEntity<Object> addPostWithImage(@RequestParam("message") String message,
                                  @RequestParam("category") Category category,

                                                   @RequestParam(value="file",required = false) MultipartFile file,
                                     @PathVariable Long id) {


  Poste poste = new Poste();
    poste.setMessage(message);
    poste.setCategory(category);
  return ResponseEntity.ok(posteService.addPostWithImage(poste, file,id));


    }
    @GetMapping("/findAll")
    public List<Poste> findAll(){
        return  posteService.getEnabledPostes();
    }
    @PostMapping("/uploadImage/{id}")
    public ResponseEntity<Object> updatePosteImage(@RequestParam("file") MultipartFile multipartFile, @PathVariable Long id) {
        Poste p = posteService.updatePosteImage(id, multipartFile);
        if (p != null) {
            // Créer un objet Map pour construire la réponse JSON
            Map<String, String> response = new HashMap<>();
            response.put("message", "User updated successfully");

            // Renvoyer la réponse JSON
            return ResponseEntity.ok(response);
        }
        // Si l'utilisateur n'est pas trouvé ou si une autre erreur se produit, renvoyer une réponse appropriée
        return ResponseEntity.badRequest().body("User not found or an error occurred");
    }
    @GetMapping("/{id}/likes")
    public ResponseEntity<Integer> getTotalLikes(@PathVariable Long id) {
        int totalLikes = posteService.getTotalLikes(id);
        return ResponseEntity.ok(totalLikes);
    }
    @GetMapping("/{id}/dislikes")
    public ResponseEntity<Integer> getTotalDislikes(@PathVariable Long id) {
        int totalDislikes = posteService.getTotalDislikes(id);
        return ResponseEntity.ok(totalDislikes);
    }

    @PatchMapping("/disable/{posteId}")
    public ResponseEntity<Void> disablePoste(@PathVariable Long posteId) {

        posteService.disablePoste(posteId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/enable/{posteId}")
    public ResponseEntity<Void> enablePoste(@PathVariable Long posteId) {

        posteService.enablePoste(posteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public Poste getPosteById(@PathVariable Long id) {
        return posteService.findPosteById(id);
    }
    @GetMapping("/count/{userId}")
    public ResponseEntity<Long> getPostCountByUserId(@PathVariable Long userId) {
        long postCount = posteService.getPostCountByUserId(userId);
        return ResponseEntity.ok(postCount);
    }
}
