package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Vérification par token facial (identifiant unique du SDK)
    Optional<Utilisateur> findByFaceToken(String faceToken);

    // Vérification par identité civile
    Optional<Utilisateur> findByNomIgnoreCaseAndPrenomIgnoreCaseAndDateNaissance(
        String nom, String prenom, String dateNaissance
    );
}
