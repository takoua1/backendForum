package com.web.forumSocialX.signale;


import com.web.forumSocialX.comment.CommentService;
import com.web.forumSocialX.poste.PosteService;
import com.web.forumSocialX.user.User;
import com.web.forumSocialX.user.UserRepository;
import com.web.forumSocialX.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service

@RequiredArgsConstructor
@Transactional
@Slf4j
public class SignaleService {
    private final UserRepository userRepository;
    private final SignaleRepository signaleRepository;
    private final PosteService posteService;
    private final CommentService commentService;
    private final UserService userService;

public Signale Signaler(Signale signale)

{
    signale.setDateSignale(new Date());
    return signaleRepository.save(signale);
}

    public List<Signale> getAllSignalementsParDate() {
        return signaleRepository.findAll(Sort.by(Sort.Direction.DESC, "dateSignale"));
    }
    @Transactional
    public ResponseEntity<List<Signale>> addDecision(Long signaleId, Decision newDecision, String adminUsername) {
        Optional<Signale> optionalSignale = signaleRepository.findById(signaleId);
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (optionalSignale.isPresent()) {
            Signale signale = optionalSignale.get();

            // Récupérer tous les signalements pour le même poste ou commentaire
            List<Signale> signalesToUpdate;
            if (signale.getPoste() != null) {
                signalesToUpdate = signaleRepository.findByPosteId(signale.getPoste().getId());
            } else if (signale.getComment() != null) {
                signalesToUpdate = signaleRepository.findByCommentId(signale.getComment().getId());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Si ni poste ni commentaire, retour d'une erreur
            }

            // Appliquer la décision à chaque signalement concerné
            for (Signale s : signalesToUpdate) {
                s.setDecision(newDecision);
                s.setAdmin(admin);
                s.setDateDecision(new Date());
                s.setEstTraite(true);

                switch (newDecision) {
                    case SUPPRIMER_POSTE:
                        if (s.getPoste() != null) {
                            posteService.disablePoste(s.getPoste().getId());
                        }
                        break;

                    case SUPPRIMER_COMMENTAIRE:
                        if (s.getComment() != null) {
                            commentService.disableComment(s.getComment().getId());
                        }
                        break;

                    case BLOQUER_UTILISATEUR:
                        blockUserFromPostOrComment(s);
                        break;

                    case SUPPRIMER_BLOQUER:
                        if (s.getPoste() != null) {
                            posteService.disablePoste(s.getPoste().getId());
                            blockUserFromPostOrComment(s);
                        }
                        if (s.getComment() != null) {
                            commentService.disableComment(s.getComment().getId());
                            blockUserFromPostOrComment(s);
                        }
                        break;

                    case NE_RIEN_FAIRE:
                        // Code pour ne rien faire (aucune action)
                        break;

                    default:
                        throw new IllegalStateException("Décision non reconnue : " + newDecision);
                }

                signaleRepository.save(s);
            }

            return new ResponseEntity<>(signalesToUpdate, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public List<Signale> listerSignale(Signale signale)

    {
        List<Signale> signalesToUpdate;
        if (signale.getPoste() != null) {
          return  signalesToUpdate = signaleRepository.findByPosteId(signale.getPoste().getId());
        } else if (signale.getComment() != null) {
          return   signalesToUpdate = signaleRepository.findByCommentId(signale.getComment().getId());
        } else {
            return null; // Si ni poste ni commentaire, retour d'une erreur
        }
    }
    private void blockUserFromPostOrComment(Signale signale) {
        if (signale.getPoste() != null) {
            User userToBlock = signale.getPoste().getUser();
            if (userToBlock != null) {
                userService.blockUser(userToBlock.getId());
            }
        }
        if (signale.getComment() != null) {
            User userToBlock = signale.getComment().getUser();
            if (userToBlock != null) {
                userService.blockUser(userToBlock.getId());
            }
        }
    }

    @Transactional
    public ResponseEntity<List<Signale>> updateDecision(Long signaleId, Decision newDecision, String adminUsername) {
        Optional<Signale> optionalSignale = signaleRepository.findById(signaleId);
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (optionalSignale.isPresent()) {
            Signale signale = optionalSignale.get();

            // Récupérer tous les signalements pour le même poste ou commentaire
            List<Signale> signalesToUpdate;
            if (signale.getPoste() != null) {
                signalesToUpdate = signaleRepository.findByPosteId(signale.getPoste().getId());
            } else if (signale.getComment() != null) {
                signalesToUpdate = signaleRepository.findByCommentId(signale.getComment().getId());
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Si ni poste ni commentaire, retour d'une erreur
            }

            // Gérer les anciennes décisions et appliquer la nouvelle pour tous les signalements liés
            for (Signale s : signalesToUpdate) {
                Decision ancienneDecision = s.getDecision();

                // Réactivation selon l'ancienne décision
                switch (ancienneDecision) {
                    case SUPPRIMER_POSTE:
                        if (s.getPoste() != null) {
                            posteService.enablePoste(s.getPoste().getId()); // Réactiver le poste
                        }
                        break;

                    case SUPPRIMER_COMMENTAIRE:
                        if (s.getComment() != null) {
                            commentService.enableComment(s.getComment().getId()); // Réactiver le commentaire
                        }
                        break;

                    case SUPPRIMER_BLOQUER:
                        if (s.getPoste() != null) {
                            posteService.enablePoste(s.getPoste().getId()); // Réactiver le poste
                        }
                        if (s.getComment() != null) {
                            commentService.enableComment(s.getComment().getId()); // Réactiver le commentaire
                        }
                        unblockUserFromPostOrComment(s); // Débloquer l'utilisateur
                        break;

                    default:
                        // Aucune action pour les autres décisions (NE_RIEN_FAIRE, etc.)
                        break;
                }

                // Appliquer la nouvelle décision
                s.setDecision(newDecision);
                s.setAdmin(admin);
                s.setDateDecision(new Date());
                s.setEstTraite(true);

                // Logique pour la nouvelle décision
                switch (newDecision) {
                    case SUPPRIMER_POSTE:
                        if (s.getPoste() != null) {
                            posteService.disablePoste(s.getPoste().getId());
                        }
                        break;

                    case SUPPRIMER_COMMENTAIRE:
                        if (s.getComment() != null) {
                            commentService.disableComment(s.getComment().getId());
                        }
                        break;

                    case BLOQUER_UTILISATEUR:
                        blockUserFromPostOrComment(s);
                        break;

                    case SUPPRIMER_BLOQUER:
                        if (s.getPoste() != null) {
                            posteService.disablePoste(s.getPoste().getId());
                            blockUserFromPostOrComment(s);
                        }
                        if (s.getComment() != null) {
                            commentService.disableComment(s.getComment().getId());
                            blockUserFromPostOrComment(s);
                        }
                        break;

                    case NE_RIEN_FAIRE:
                        // Aucune action à prendre pour cette décision
                        break;

                    default:
                        throw new IllegalStateException("Décision non reconnue : " + newDecision);
                }

                signaleRepository.save(s);
            }

            return new ResponseEntity<>(signalesToUpdate, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private void unblockUserFromPostOrComment(Signale signale) {
        if (signale.getPoste() != null) {
            User userToBlock = signale.getPoste().getUser();
            if (userToBlock != null) {
                userService.unblockUser(userToBlock.getId());
            }
        }
        if (signale.getComment() != null) {
            User userToBlock = signale.getComment().getUser();
            if (userToBlock != null) {
                userService.unblockUser(userToBlock.getId());
            }
        }
    }

    public List<Signale> getSignalesByPosteOrCommentUser(Long userId) {
        List<Signale> allSignales = findAllEnabledSignales();
        return allSignales.stream()
                .filter(signale -> (signale.getPoste() != null && signale.getPoste().getUser().getId().equals(userId)) ||
                        (signale.getComment() != null && signale.getComment().getUser().getId().equals(userId)))
                .collect(Collectors.toList());
    }

   public Signale disableSignale(Long id)
   {
       Signale signale = signaleRepository.findById(id).orElseThrow(() -> new RuntimeException("Signale not found"));

       signale.setEnabled(false);
      return signaleRepository.save(signale);
   }
    public List<Signale> findAllEnabledSignales() {
        Sort sort = Sort.by(
                Sort.Order.asc("estTraite"),  // Les enregistrements avec estTraite = false en premier
                Sort.Order.desc("dateSignale") // Tri décroissant par dateSignale
        );
        return signaleRepository.findByEnabledTrue(sort);
    }

    public void deleteSignale(Long id)
    {
        Signale signale = signaleRepository.findById(id).orElseThrow(() -> new RuntimeException("Signale not found"));
        signaleRepository.delete(signale);
    }
}
