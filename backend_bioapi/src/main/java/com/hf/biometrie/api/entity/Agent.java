package com.hf.biometrie.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "idutilisateur")
    private Long idutilisateur;

    private Boolean changepassword;
    private LocalDateTime createdat;
    private Long createdby;
    private LocalDateTime datecreation;
    private LocalDateTime deletedat;
    private Long deletedby;
    private Boolean isblocked;
    private Boolean isdeleted;
    private LocalDateTime lastmodifiedat;
    private Long lastmodifiedby;

    @JsonIgnore
    private String motdepasse;

    private String nom;
    private String nomutilisateur;
    private String prenom;
    private String typeutilisateur;

    private Boolean blocageutilisateur;
    private Boolean changementmotdepasseutilisateur;
    private String contact1utilisateur;
    private String contact2utilisateur;
    private LocalDateTime dateheureblocageutilisateur;
    private LocalDateTime datenaissanceutilisateur;
    private String genreutilisateur;
    private String lieunaissanceutilisateur;
    private String loginutilisateur;

    @JsonIgnore
    private String motdepasseutilisateur;

    private Integer nbtentativeechoue;
    private String prenomutilisateur;
    private String raisonblocageutilisateur;
    private String situationmatrimonialeutilisateur;

    @JsonIgnore
    private byte[] annulairedroiutilisateur;
    @JsonIgnore
    private byte[] annulairegaucheutilisateur;
    @JsonIgnore
    private byte[] auriculairedroitutilisateur;
    @JsonIgnore
    private byte[] auriculairegaucheutilisateur;
    @JsonIgnore
    private byte[] indexdroitutilisateur;
    @JsonIgnore
    private byte[] indexgaucheutilisateur;
    @JsonIgnore
    private byte[] majeurdroitutilisateur;
    @JsonIgnore
    private byte[] majeurgaucheutilisateur;
    @JsonIgnore
    private byte[] poucedroitutilisateur;
    @JsonIgnore
    private byte[] poucegaucheutilisateur;
    @JsonIgnore
    private byte[] photographieutilisateur;
}
