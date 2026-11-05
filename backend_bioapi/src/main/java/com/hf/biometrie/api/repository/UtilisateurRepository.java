package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByFaceToken(String faceToken);

    Optional<Utilisateur> findByNomIgnoreCaseAndPrenomIgnoreCaseAndDateNaissance(
        String nom, String prenom, String dateNaissance
    );

    boolean existsBySourceIdentificationId(Long sourceIdentificationId);

    interface UtilisateurRow {
        Long getId();
        String getUin();
        String getMatricule();
        String getNom();
        String getNomJeuneFille();
        String getPrenom();
        String getSexe();
        String getDateNaissance();
        String getLieuNaissance();
        String getLocalite();
        String getVillage();
        String getSituationMatrimoniale();
        String getCategorie();
        String getEmploiTenu();
        String getDernierDiplome();
        String getGroupeSanguin();
        LocalDate getDateCnib();
        String getContact1();
        String getNomEquipement();
        String getStatut();
        Long getSourceIdentificationId();
        String getFacePhotoBase64();
        LocalDateTime getDateCreation();
        long getNbEmpreintes();
        long getNbIris();
        String getFirstDeviceModel();
        Integer getFirstScoreNfiq();
    }

    @Query("""
        SELECT u.id AS id,
               u.uin AS uin,
               u.matricule AS matricule,
               u.nom AS nom,
               u.nomJeuneFille AS nomJeuneFille,
               u.prenom AS prenom,
               u.sexe AS sexe,
               u.dateNaissance AS dateNaissance,
               u.lieuNaissance AS lieuNaissance,
               u.localite AS localite,
               u.village AS village,
               u.situationMatrimoniale AS situationMatrimoniale,
               u.categorie AS categorie,
               u.emploiTenu AS emploiTenu,
               u.dernierDiplome AS dernierDiplome,
               u.groupeSanguin AS groupeSanguin,
               u.dateCnib AS dateCnib,
               u.contact1 AS contact1,
               u.nomEquipement AS nomEquipement,
               u.statut AS statut,
               u.sourceIdentificationId AS sourceIdentificationId,
               u.facePhotoBase64 AS facePhotoBase64,
               u.dateCreation AS dateCreation,
               (SELECT COUNT(e) FROM Empreinte e WHERE e.utilisateur = u) AS nbEmpreintes,
               (SELECT COUNT(ir) FROM Iris ir WHERE ir.utilisateur = u) AS nbIris,
               (SELECT MAX(e.deviceModel) FROM Empreinte e WHERE e.utilisateur = u) AS firstDeviceModel,
               (SELECT MIN(e.scoreNfiq) FROM Empreinte e WHERE e.utilisateur = u) AS firstScoreNfiq
        FROM Utilisateur u
        ORDER BY u.id ASC
        """)
    List<UtilisateurRow> findAllSummaries();
}
