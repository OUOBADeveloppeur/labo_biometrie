package com.hf.biometrie.api.controller;

import com.hf.biometrie.api.dto.EnrollmentRequest;
import com.hf.biometrie.api.entity.Utilisateur;
import com.hf.biometrie.api.exception.DoublonException;
import com.hf.biometrie.api.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/enroll")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> enrollUser(@RequestBody EnrollmentRequest request) {
        try {
            Utilisateur savedUser = enrollmentService.saveEnrollment(request);

            // ── Succès ──
            Map<String, Object> success = new LinkedHashMap<>();
            success.put("status", "success");
            success.put("id", savedUser.getId());
            return ResponseEntity.ok(success);

        } catch (DoublonException e) {
            // ── Doublon détecté : 409 Conflict avec les détails de la personne existante ──
            Utilisateur existant = e.getUtilisateurExistant();

            Map<String, Object> details = new LinkedHashMap<>();
            details.put("id", existant.getId());
            details.put("nom", existant.getNom() != null ? existant.getNom() : "");
            details.put("prenom", existant.getPrenom() != null ? existant.getPrenom() : "");
            details.put("sexe", existant.getSexe() != null ? existant.getSexe() : "");
            details.put("dateNaissance", existant.getDateNaissance() != null ? existant.getDateNaissance() : "");
            details.put("localite", existant.getLocalite() != null ? existant.getLocalite() : "");
            details.put("nbEmpreintes", existant.getEmpreintes() != null ? existant.getEmpreintes().size() : 0);
            details.put("nbIris", existant.getIris() != null ? existant.getIris().size() : 0);
            details.put("dateEnrolement", existant.getDateCreation() != null ? existant.getDateCreation().toString() : null);

            Map<String, Object> doublon = new LinkedHashMap<>();
            doublon.put("status", "doublon");
            doublon.put("raison", e.getRaisonDoublon());
            doublon.put("similarite", "100%");
            doublon.put("message", "Cette personne est déjà enregistrée dans le système.");
            doublon.put("existant", details);

            return ResponseEntity.status(HttpStatus.CONFLICT).body(doublon);

        } catch (Exception e) {
            // ── Erreur serveur ──
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

