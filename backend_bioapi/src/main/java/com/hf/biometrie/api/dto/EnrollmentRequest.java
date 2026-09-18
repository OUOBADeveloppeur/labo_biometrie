package com.hf.biometrie.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class EnrollmentRequest {

    // ── Identité civile ──
    private String nom;
    private String prenom;
    private String sexe;
    private String dateNaissance;
    private String localite;
    private String faceToken;
    private String facePhotoBase64;

    // ── Métadonnées de session (générées automatiquement côté Android) ──
    private String nomEquipement;         // Nom du terminal : Build.MODEL (ex: "TR760")
    private Long tempsEnrolementTotalMs;  // Durée totale de la session en ms
    private String statut;                // COMPLET | PARTIEL | ECHEC

    private List<BiometricTemplate> empreintes;
    private List<BiometricTemplate> iris;

    @Data
    public static class BiometricTemplate {

        // ── Identification ──
        private String position;           // Clé technique : left_0, right_3, thumb_1...
        private String categorieDoigt;     // Libellé lisible : INDEX_G, POUCE_D, AURICULAIRE_D...
        private String typeCapture;        // SLAP_GAUCHE | SLAP_DROIT | POUCES

        // ── Données biométriques ──
        private String templateBase64;     // Gabarit ISO/IEC 19794-2
        private String imageBase64;        // Image BMP en Base64

        // ── Équipement (constantes du terminal) ──
        private String deviceModel;        // Modèle du terminal (ex: TR760)
        private String firmwareVersion;    // Version SDK/firmware (ex: FAP60-SDK-2.x)
        private Integer resolution;        // Résolution DPI (ex: 500)

        // ── Qualité (source SDK, automatique) ──
        private Integer qualite;           // NFIQ brut 1–5 (1=parfait)
        private Integer scoreNfiq;         // NFIQ ISO/IEC 29794-4 explicite
        private Integer scoreQualiteSdk;   // Score normalisé 0–100 : (5-nfiq)/4×100
        private Integer surfaceContact;    // Surface de contact en pixels (area SDK)

        // ── Performance (mesurée automatiquement) ──
        private Long tempsCaptureMs;       // Durée de capture en ms
        private Integer nombreTentatives;  // Nb de tentatives avant succès
    }
}
