package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.CasAdjudication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CasAdjudicationRepository extends JpaRepository<CasAdjudication, Long> {

    List<CasAdjudication> findAllByOrderByDateDetectionDesc();

    long countByStatut(String statut);

    @Query("""
        SELECT c FROM CasAdjudication c
        WHERE (c.candidat.id = :idA AND c.existant.id = :idB)
           OR (c.candidat.id = :idB AND c.existant.id = :idA)
        """)
    Optional<CasAdjudication> findExistingPair(Long idA, Long idB);
}

