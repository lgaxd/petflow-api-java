package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskLevelRepository extends JpaRepository<RiskLevel, Long> {

    Optional<RiskLevel> findByNameIgnoreCase(String name);
}