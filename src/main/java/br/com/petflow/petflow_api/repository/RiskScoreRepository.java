package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

    List<RiskScore> findByPetId(Long petId);
}