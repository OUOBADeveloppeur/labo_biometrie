package com.hf.biometrie.api.repository;

import com.hf.biometrie.api.entity.Iris;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IrisRepository extends JpaRepository<Iris, Long> {
}
