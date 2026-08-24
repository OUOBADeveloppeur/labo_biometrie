package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Empreinte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpreinteRepository extends JpaRepository<Empreinte, Long> {
}
