package com.hf.biometrie.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
public class Utilisateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    private String sexe;
    private String dateNaissance;
    private String localite;
    private String faceToken;

    @Column(columnDefinition = "TEXT")
    private String facePhotoBase64;

    // ── Métadonnées de session (automatiques) ──
    private String nomEquipement;            // Nom/modèle du terminal Android (Build.MODEL)
    private Long tempsEnrolementTotalMs;     // Durée totale de la session en ms
    private String statut;                   // COMPLET | PARTIEL | ECHEC

    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Empreinte> empreintes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Iris> iris = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }
}
