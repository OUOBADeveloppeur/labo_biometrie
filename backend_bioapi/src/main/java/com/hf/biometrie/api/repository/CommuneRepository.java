package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommuneRepository extends JpaRepository<Commune, Long> {
}

