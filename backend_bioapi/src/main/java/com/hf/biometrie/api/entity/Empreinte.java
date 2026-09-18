package com.hf.biometrie.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "empreintes_digitales")
@Data
@NoArgsConstructor
public class Empreinte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identification ──
    private String positionDoigt;       // Clé technique : left_0, right_3, thumb_1...
    private String categorieDoigt;      // Libellé lisible : INDEX_G, POUCE_D, AURICULAIRE_D...
    private String typeCapture;         // Mode de capture : SLAP_GAUCHE, SLAP_DROIT, POUCES

    // ── Données biométriques ──
    @Column(columnDefinition = "TEXT")
    private String templateBase64;      // Gabarit ISO/IEC 19794-2

    @Column(columnDefinition = "TEXT")
    private String imageBase64;         // Image BMP en Base64

    // ── Équipement ──
    private String deviceModel;         // Modèle du terminal (ex: TR760)
    private String firmwareVersion;     // Version SDK/firmware (ex: FAP60-SDK-2.x)
    private Integer resolution;         // Résolution DPI (ex: 500)

    // ── Qualité (automatique — source SDK) ──
    private Integer qualite;            // Score NFIQ brut (1=parfait, 5=mauvais)
    private Integer scoreNfiq;          // Score NFIQ ISO/IEC 29794-4 explicite (1–5)
    private Integer scoreQualiteSdk;    // Score normalisé 0–100 : (5-nfiq)/4*100
    private Integer surfaceContact;     // Surface de contact en pixels (area SDK)

    // ── Performance (automatique — mesuré par chrono) ──
    private Long tempsCaptureMs;        // Durée de la capture en ms
    private Integer nombreTentatives;   // Nb de tentatives avant capture réussie

    private LocalDateTime dateCapture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    @JsonIgnore
    private Utilisateur utilisateur;

    @PrePersist
    protected void onCreate() {
        dateCapture = LocalDateTime.now();
    }
}
