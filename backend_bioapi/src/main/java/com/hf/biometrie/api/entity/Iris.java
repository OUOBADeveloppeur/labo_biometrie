package com.hf.biometrie.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "iris")
@Data
@NoArgsConstructor
public class Iris {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String positionOeil;

    @Column(columnDefinition = "TEXT")
    private String templateBase64;

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
