package com.hf.biometrie.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Entity
@Table(name = "identification")
@Data
@NoArgsConstructor
public class Identification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categorie;
    private String commune;

    @Column(name = "contact_1")
    private String contact1;

    @Column(name = "contact_2")
    private String contact2;

    @Column(name = "contact_3")
    private String contact3;

    @Column(name = "contact_pers_prev_1")
    private String contactPersPrev1;

    @Column(name = "contact_pers_prev_2")
    private String contactPersPrev2;

    private LocalDateTime createdat;
    private Long createdby;
    private LocalDateTime deletedat;
    private Long deletedby;
    private String dernierdiplome;

    @Column(name = "dt_cnib")
    private LocalDate dtCnib;

    @Column(name = "dt_naissance")
    private LocalDate dtNaissance;

    @Column(name = "emploi_tenu")
    private String emploiTenu;

    private String etat;
    private String groupesanguin;
    private Boolean isdeleted;
    private LocalDateTime lastmodifiedat;
    private Long lastmodifiedby;

    @Column(name = "lieu_naiss")
    private String lieuNaiss;

    @Column(name = "localite_origine")
    private String localiteOrigine;

    private String matricule;

    @Column(name = "nb_enfant")
    private Long nbEnfant;

    private Long nbfemme;

    @Column(name = "niveau_instruct")
    private String niveauInstruct;

    @Column(name = "no_nip")
    private String noNip;

    private String nom;

    @Column(name = "nom_jf")
    private String nomJf;

    private String numeroarme;
    private String numerochassis;

    @Column(name = "personne_prev_1")
    private String personnePrev1;

    @Column(name = "personne_prev_2")
    private String personnePrev2;

    private String prenoms;
    private String promotion;
    private String sexe;

    @Column(name = "situat_matrimo")
    private String situatMatrimo;

    private Long situationprime;
    private String village;
    private Long idcommune;

    @Column(name = "autre_niveau_instruct")
    private String autreNiveauInstruct;

    private Long idcommunenaissance;
    private Long idpays;
    private Boolean isfingerprinted;

    @JsonIgnore
    private byte[] photographie;

    @JsonIgnore
    private byte[] empreinteannulairedroit;
    @JsonIgnore
    private byte[] empreinteannulairegauche;
    @JsonIgnore
    private byte[] empreinteauriculairedroit;
    @JsonIgnore
    private byte[] empreinteauriculairegauche;
    @JsonIgnore
    private byte[] empreinteindexdroit;
    @JsonIgnore
    private byte[] empreinteindexgauche;
    @JsonIgnore
    private byte[] empreintemajeurdroit;
    @JsonIgnore
    private byte[] empreintemajeurgauche;
    @JsonIgnore
    private byte[] empreintepoucedroit;
    @JsonIgnore
    private byte[] empreintepoucegauche;

    @JsonIgnore
    private byte[] empreinte2pouces;
    @JsonIgnore
    @Column(name = "empreinte2poucesimageansiincits_381_2004")
    private byte[] empreinte2poucesimageansiincits3812004;
    @JsonIgnore
    @Column(name = "empreinte2poucesimageiso19794_4_2011")
    private byte[] empreinte2poucesimageiso1979442011;
    @JsonIgnore
    @Column(name = "empreinte2poucestemplateansiincits_378_2004")
    private byte[] empreinte2poucestemplateansiincits3782004;
    @JsonIgnore
    @Column(name = "empreinte2poucestemplateiso19794_2_2011")
    private byte[] empreinte2poucestemplateiso1979422011;

    @JsonIgnore
    private byte[] empreinte4ddroit;
    @JsonIgnore
    @Column(name = "empreinte4ddroitimageansiincits_381_2004")
    private byte[] empreinte4ddroitimageansiincits3812004;
    @JsonIgnore
    @Column(name = "empreinte4ddroitimageiso19794_4_2011")
    private byte[] empreinte4ddroitimageiso1979442011;
    @JsonIgnore
    @Column(name = "empreinte4ddroittemplateansiincits_378_2004")
    private byte[] empreinte4ddroittemplateansiincits3782004;
    @JsonIgnore
    @Column(name = "empreinte4ddroittemplateiso19794_2_2011")
    private byte[] empreinte4ddroittemplateiso1979422011;

    @JsonIgnore
    private byte[] empreinte4dgauche;
    @JsonIgnore
    @Column(name = "empreinte4dgaucheimageansiincits_381_2004")
    private byte[] empreinte4dgaucheimageansiincits3812004;
    @JsonIgnore
    @Column(name = "empreinte4dgaucheimageiso19794_4_2011")
    private byte[] empreinte4dgaucheimageiso1979442011;
    @JsonIgnore
    @Column(name = "empreinte4dgauchetemplateansiincits_378_2004")
    private byte[] empreinte4dgauchetemplateansiincits3782004;
    @JsonIgnore
    @Column(name = "empreinte4dgauchetemplateiso19794_2_2011")
    private byte[] empreinte4dgauchetemplateiso1979422011;

    public static String byteaToDataUri(byte[] raw) {
        if (raw == null || raw.length == 0) return null;
        String asText = new String(raw, StandardCharsets.UTF_8).trim();
        if (asText.equalsIgnoreCase("binary data") || asText.length() < 16) {
            return null;
        }
        if (asText.startsWith("data:image/")) {
            return asText;
        }
        if (asText.startsWith("iVBOR")) {
            return "data:image/png;base64," + asText;
        }
        if (asText.startsWith("/9j/")) {
            return "data:image/jpeg;base64," + asText;
        }
        if (asText.startsWith("Qk")) {
            return "data:image/bmp;base64," + asText;
        }
        if ((raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xD8) {
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(raw);
        }
        if ((raw[0] & 0xFF) == 0x89 && raw[1] == 0x50) {
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(raw);
        }
        if (raw[0] == 'B' && raw[1] == 'M') {
            return "data:image/bmp;base64," + Base64.getEncoder().encodeToString(raw);
        }
        return null;
    }
}
