package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.RiskLevelResponseDTO;
import br.com.petflow.petflow_api.entity.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RiskLevelRepository extends JpaRepository<RiskLevel, Long> {

    Optional<RiskLevel> findByNameIgnoreCase(String name);
    
    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.RiskLevelResponseDTO(
                rl.id, rl.name, rl.description, rl.minScore, rl.maxScore
            )
            FROM RiskLevel rl
            """)
    Page<RiskLevelResponseDTO> findAllProjected(Pageable pageable);
}