package com.hf.biometrie.api.controller;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.CasAdjudication;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.repository.CasAdjudicationRepository;
import com.hf.biometrie.api.repository.UtilisateurRepository;
import com.hf.biometrie.api.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enroll")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UtilisateurRepository utilisateurRepository;
    private final CasAdjudicationRepository casAdjudicationRepository;

    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAllEnrollments() {
        return ResponseEntity.ok(utilisateurRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEnrollmentById(@PathVariable Long id) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findById(id);
        if (utilisateur.isPresent()) {
            return ResponseEntity.ok(utilisateur.get());
        } else {
            Map<String, Object> notFound = new LinkedHashMap<>();
            notFound.put("status", "error");
            notFound.put("message", "Aucun enrôlement trouvé avec l'id : " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> enrollUser(@RequestBody EnrollmentRequest request) {
        try {
            EnrollmentService.EnrollmentOutcome outcome = enrollmentService.saveEnrollmentWithAbis(request);
            Utilisateur savedUser = outcome.utilisateur();
            List<CasAdjudication> cases = outcome.casAdjudications();

            if (!cases.isEmpty()) {
                CasAdjudication top = cases.get(0);
                Utilisateur existant = top.getExistant();

                Map<String, Object> details = new LinkedHashMap<>();
                details.put("id", existant.getId());
                details.put("uin", existant.getUin());
                details.put("nom", existant.getNom() != null ? existant.getNom() : "");
                details.put("prenom", existant.getPrenom() != null ? existant.getPrenom() : "");
                details.put("sexe", existant.getSexe() != null ? existant.getSexe() : "");
                details.put("dateNaissance", existant.getDateNaissance() != null ? existant.getDateNaissance() : "");
                details.put("localite", existant.getLocalite() != null ? existant.getLocalite() : "");

                Map<String, Object> doublon = new LinkedHashMap<>();
                doublon.put("status", "doublon");
                doublon.put("adjudicationId", top.getId());
                doublon.put("candidatId", savedUser.getId());
                doublon.put("raison", top.getRaisonAlerte());
                doublon.put("scoreAbis", top.getScoreAbis());
                doublon.put("similarite", top.getScoreAbis() != null && top.getScoreAbis() > 0 ? (top.getScoreAbis() + " pts ABIS") : "100%");
                doublon.put("message", "Doublon potentiel détecté — dossier envoyé en Adjudication Manuelle (ABIS).");
                doublon.put("existant", details);

                return ResponseEntity.status(HttpStatus.CONFLICT).body(doublon);
            }

            Map<String, Object> success = new LinkedHashMap<>();
            success.put("status", "success");
            success.put("id", savedUser.getId());
            success.put("uin", savedUser.getUin());
            return ResponseEntity.ok(success);

        } catch (Exception e) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteEnrollment(@PathVariable Long id) {
        if (!utilisateurRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        casAdjudicationRepository.findAll().stream()
                .filter(c -> c.getCandidat().getId().equals(id) || c.getExistant().getId().equals(id))
                .forEach(casAdjudicationRepository::delete);
        utilisateurRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEnrollment(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Optional<Utilisateur> optUser = utilisateurRepository.findById(id);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Utilisateur user = optUser.get();
        if (payload.containsKey("nom")) user.setNom(payload.get("nom"));
        if (payload.containsKey("prenom")) user.setPrenom(payload.get("prenom"));
        if (payload.containsKey("sexe")) user.setSexe(payload.get("sexe"));
        if (payload.containsKey("dateNaissance")) user.setDateNaissance(payload.get("dateNaissance"));
        if (payload.containsKey("localite")) user.setLocalite(payload.get("localite"));
        if (payload.containsKey("emploiTenu")) user.setEmploiTenu(payload.get("emploiTenu"));
        if (payload.containsKey("contact1")) user.setContact1(payload.get("contact1"));
        if (payload.containsKey("groupeSanguin")) user.setGroupeSanguin(payload.get("groupeSanguin"));
        utilisateurRepository.save(user);
        return ResponseEntity.ok(user);
    }
}
