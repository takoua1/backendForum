package com.web.forumTunisia.comment;


import com.web.forumTunisia.poste.Poste;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(path = "/comment")
@RequiredArgsConstructor
public class CommentController {


    public final CommentService commentService;


    @PostMapping("/addComment")
    public ResponseEntity<?> addCommentToPoste(@RequestBody Comment comm) {
        Comment comment = commentService.addCommentToPoste(comm, comm.getPoste(), comm.getUser());
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/addReponse")
    public ResponseEntity<Comment> addRepliesToComment(@RequestBody Comment commentChild) {


        return ResponseEntity.ok(commentService.addCommentToComment(commentChild.getParentComment(), commentChild, commentChild.getUser()));
    }

    @PostMapping("/addCommentToPosteWithImage/{idUser}/{idPoste}")
    public ResponseEntity<Object> addCommentToPosteWithImage(@RequestParam("text") String text,
                                                             @RequestParam(value = "file", required = false) MultipartFile file,
                                                             @PathVariable Long idUser, Long idPoste) {


        Comment comment = new Comment();
        comment.setText(text);

        return ResponseEntity.ok(commentService.addCommentToPosteWithImage(comment, file, idUser, idPoste));


    }

    @PostMapping("/addCommentToCommentWithImage/{idUser}/{idPidComm}")
    public ResponseEntity<Object> addCommentToCommentWithImage(@RequestParam("text") String text,
                                                               @RequestParam(value = "file", required = false) MultipartFile file,
                                                               @PathVariable Long idUser, Long idComm) {


        Comment comment = new Comment();
        comment.setText(text);

        return ResponseEntity.ok(commentService.addCommentToCommentWithImage(comment, file, idUser, idComm));


    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteComment(@PathVariable Long id) {
        Comment comment = commentService.deleteComment(id);
        if (comment != null) {
            return ResponseEntity.ok("Comment supprimer avec succès");
        }
        return null;
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Integer> getTotalLikes(@PathVariable Long id) {
        int totalLikes = commentService.getTotalLikes(id);
        return ResponseEntity.ok(totalLikes);
    }
    @GetMapping("/{id}/dislikes")
    public ResponseEntity<Integer> getTotalDislikes(@PathVariable Long id) {
        int totalDislikes = commentService.getTotalDislikes(id);
        return ResponseEntity.ok(totalDislikes);
    }

    @GetMapping("/poste/{commentId}")
    public ResponseEntity<Poste> getPosteByCommentId(@PathVariable Long commentId) {
        Poste poste = commentService.findPosteByCommentId(commentId);
        if (poste != null) {
            return ResponseEntity.ok(poste);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    public ResponseEntity<Comment> getParentComment(@PathVariable Long childCommentId) {
        Comment parentComment = commentService.findParentCommentWithPoste(childCommentId);
        if (parentComment != null) {
            return ResponseEntity.ok(parentComment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/disable/{commentId}")
    public ResponseEntity<Void> disableComment(@PathVariable Long commentId)
    {
        commentService.disableComment(commentId);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/enable/{commentId}")
    public ResponseEntity<Void> enableComment(@PathVariable Long commentId)
    {
        commentService.enableComment(commentId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public Comment getCommentById(@PathVariable Long id) {
        return commentService.findCommentById(id);
    }

    @GetMapping("/poste/comments/{postId}")
    public List<Comment> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return comments;
    }

    @GetMapping("/child/{id}")
    public ResponseEntity<List<Comment>> getChildComments(@PathVariable Long id) {
        List<Comment> childComments = commentService.getChildComments(id);
        return ResponseEntity.ok(childComments);
    }
    @GetMapping("/with-parent/{id}")
    public ResponseEntity<Comment> getCommentWithParent(@PathVariable Long id) {
        Comment comment = commentService.getCommentWithParent(id);
        return ResponseEntity.ok(comment);
    }
}
