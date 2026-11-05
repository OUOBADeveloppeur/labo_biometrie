package com.hf.biometrie.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    // ── Identité principale & Biométrie faciale ──
    private String uin;                      // UIN généré ou N° NIP
    private String matricule;
    private String nom;
    private String nomJeuneFille;
    private String prenom;
    private String sexe;
    private String dateNaissance;
    private String lieuNaissance;
    private String localite;                 // Commune / Ville de résidence
    private String localiteOrigine;
    private String village;

    private Long idCommune;
    private Long idCommuneNaissance;
    private Long idPays;

    private String faceToken;

    @Column(columnDefinition = "TEXT")
    private String facePhotoBase64;

    // ── État civil, Profession & Documents (unifiés depuis Identification) ──
    private String situationMatrimoniale;
    private Long nbEpouses;
    private Long nbEnfants;
    private String categorie;
    private String promotion;
    private String emploiTenu;
    private String niveauInstruction;
    private String autreNiveauInstruction;
    private String dernierDiplome;
    private String groupeSanguin;
    private LocalDate dateCnib;
    private Long situationPrime;
    private String numeroArme;
    private String numeroChassis;

    // ── Contacts & Personnes à prévenir ──
    private String contact1;
    private String contact2;
    private String contact3;
    private String personnePrevenir1;
    private String contactPersonnePrevenir1;
    private String personnePrevenir2;
    private String contactPersonnePrevenir2;

    // ── Métadonnées de session & Traçabilité ──
    private String nomEquipement;            // ex: TR760 ou Import Base Identification
    private Long tempsEnrolementTotalMs;
    private String statut;                   // COMPLET | Opérationnel | PARTIEL | ADJUDICATION

    @Column(unique = true)
    private Long sourceIdentificationId;     // ID d'origine dans la table identification (anti-doublon import)

    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Empreinte> empreintes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Iris> iris = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
