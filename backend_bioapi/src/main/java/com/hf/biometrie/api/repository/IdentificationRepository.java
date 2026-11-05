package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Identification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IdentificationRepository extends JpaRepository<Identification, Long> {

    interface IdentificationRow {
        Long getId();
        String getNoNip();
        String getMatricule();
        String getNom();
        String getNomJf();
        String getPrenoms();
        String getSexe();
        LocalDate getDtNaissance();
        String getLieuNaiss();
        String getLocaliteOrigine();
        String getSituatMatrimo();
        Long getNbEnfant();
        Long getNbfemme();
        String getCategorie();
        String getPromotion();
        String getEmploiTenu();
        String getNiveauInstruct();
        String getAutreNiveauInstruct();
        String getDernierdiplome();
        String getGroupesanguin();
        LocalDate getDtCnib();
        String getEtat();
        String getCommune();
        String getVillage();
        Long getIdcommune();
        Long getIdcommunenaissance();
        Long getIdpays();
        String getContact1();
        String getContact2();
        String getContact3();
        String getPersonnePrev1();
        String getContactPersPrev1();
        String getPersonnePrev2();
        String getContactPersPrev2();
        String getNumeroarme();
        String getNumerochassis();
        Long getSituationprime();
        Boolean getIsfingerprinted();
        Boolean getHasPhoto();
        LocalDateTime getCreatedat();
    }

    @Query("""
        SELECT i.id AS id,
               i.noNip AS noNip,
               i.matricule AS matricule,
               i.nom AS nom,
               i.nomJf AS nomJf,
               i.prenoms AS prenoms,
               i.sexe AS sexe,
               i.dtNaissance AS dtNaissance,
               i.lieuNaiss AS lieuNaiss,
               i.localiteOrigine AS localiteOrigine,
               i.situatMatrimo AS situatMatrimo,
               i.nbEnfant AS nbEnfant,
               i.nbfemme AS nbfemme,
               i.categorie AS categorie,
               i.promotion AS promotion,
               i.emploiTenu AS emploiTenu,
               i.niveauInstruct AS niveauInstruct,
               i.autreNiveauInstruct AS autreNiveauInstruct,
               i.dernierdiplome AS dernierdiplome,
               i.groupesanguin AS groupesanguin,
               i.dtCnib AS dtCnib,
               i.etat AS etat,
               i.commune AS commune,
               i.village AS village,
               i.idcommune AS idcommune,
               i.idcommunenaissance AS idcommunenaissance,
               i.idpays AS idpays,
               i.contact1 AS contact1,
               i.contact2 AS contact2,
               i.contact3 AS contact3,
               i.personnePrev1 AS personnePrev1,
               i.contactPersPrev1 AS contactPersPrev1,
               i.personnePrev2 AS personnePrev2,
               i.contactPersPrev2 AS contactPersPrev2,
               i.numeroarme AS numeroarme,
               i.numerochassis AS numerochassis,
               i.situationprime AS situationprime,
               i.isfingerprinted AS isfingerprinted,
               CASE WHEN i.photographie IS NOT NULL THEN true ELSE false END AS hasPhoto,
               i.createdat AS createdat
        FROM Identification i
        ORDER BY i.id ASC
        """)
    List<IdentificationRow> findAllSummaries();
}
