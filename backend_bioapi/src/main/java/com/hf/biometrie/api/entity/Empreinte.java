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

    private String positionDoigt;

    @Column(columnDefinition = "TEXT")
    private String templateBase64;

    @Column(columnDefinition = "TEXT")
    private String imageBase64;

    private String deviceModel; // Nom / modèle de l'équipement de capture

    private Integer qualite; // Score de qualité de l'empreinte (ex: NFIQ ou score SDK)

    private Long tempsCaptureMs; // Temps d'acquisition en millisecondes

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
