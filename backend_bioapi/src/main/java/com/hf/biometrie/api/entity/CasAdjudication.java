package com.hf.biometrie.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cas_adjudication")
@Data
@NoArgsConstructor
public class CasAdjudication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Utilisateur candidat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "existant_id", nullable = false)
    private Utilisateur existant;

    private Double scoreAbis;             // Score SourceAFIS (ex: >= 40 = forte correspondance d'empreinte)
    private Double scoreIdentite;         // Score démographique (0 - 100%)
    private String modalite;              // EMPREINTE_ABIS | TOKEN_FACIAL | IDENTITE_CIVILE | MULTIMODAL
    private String doigtCandidat;         // ex: right_0
    private String doigtExistant;         // ex: right_0
    private String raisonAlerte;

    @Column(columnDefinition = "TEXT")
    private String detailsComparaisonJson;

    private String statut;                // EN_ATTENTE | DOUBLON_CONFIRME | DIFFERENTS_VALIDES
    private String decisionPar;           // Nom de l'agent opérateur ayant statué

    @Column(columnDefinition = "TEXT")
    private String commentaireDecision;

    private LocalDateTime dateDetection;
    private LocalDateTime dateDecision;

    @PrePersist
    protected void onCreate() {
        if (dateDetection == null) {
            dateDetection = LocalDateTime.now();
        }
        if (statut == null) {
            statut = "EN_ATTENTE";
        }
    }
}

