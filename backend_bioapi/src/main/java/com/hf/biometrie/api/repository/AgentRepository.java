package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AgentRepository extends JpaRepository<Agent, Integer> {

    interface AgentRow {
        Integer getId();
        Long getIdutilisateur();
        String getNom();
        String getPrenom();
        String getPrenomutilisateur();
        String getNomutilisateur();
        String getLoginutilisateur();
        String getTypeutilisateur();
        String getGenreutilisateur();
        LocalDateTime getDatenaissanceutilisateur();
        String getLieunaissanceutilisateur();
        String getSituationmatrimonialeutilisateur();
        String getContact1utilisateur();
        String getContact2utilisateur();
        Boolean getIsblocked();
        Boolean getBlocageutilisateur();
        String getRaisonblocageutilisateur();
        LocalDateTime getDateheureblocageutilisateur();
        Integer getNbtentativeechoue();
        Boolean getChangepassword();
        Boolean getIsdeleted();
        LocalDateTime getCreatedat();
    }

    @Query("""
        SELECT a.id AS id,
               a.idutilisateur AS idutilisateur,
               a.nom AS nom,
               a.prenom AS prenom,
               a.prenomutilisateur AS prenomutilisateur,
               a.nomutilisateur AS nomutilisateur,
               a.loginutilisateur AS loginutilisateur,
               a.typeutilisateur AS typeutilisateur,
               a.genreutilisateur AS genreutilisateur,
               a.datenaissanceutilisateur AS datenaissanceutilisateur,
               a.lieunaissanceutilisateur AS lieunaissanceutilisateur,
               a.situationmatrimonialeutilisateur AS situationmatrimonialeutilisateur,
               a.contact1utilisateur AS contact1utilisateur,
               a.contact2utilisateur AS contact2utilisateur,
               a.isblocked AS isblocked,
               a.blocageutilisateur AS blocageutilisateur,
               a.raisonblocageutilisateur AS raisonblocageutilisateur,
               a.dateheureblocageutilisateur AS dateheureblocageutilisateur,
               a.nbtentativeechoue AS nbtentativeechoue,
               a.changepassword AS changepassword,
               a.isdeleted AS isdeleted,
               a.createdat AS createdat
        FROM Agent a
        ORDER BY a.id ASC
        """)
    List<AgentRow> findAllSummaries();
}
