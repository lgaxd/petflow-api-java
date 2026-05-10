package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.RiskScoreResponseDTO;
import br.com.petflow.petflow_api.entity.RiskScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RiskScoreRepository extends JpaRepository<RiskScore, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.RiskScoreResponseDTO(
                rs.id, rs.score, rs.calculatedAt,
                rs.pet.id, rs.pet.name,
                rs.riskLevel.id, rs.riskLevel.name
            )
            FROM RiskScore rs
            WHERE rs.pet.id = :petId
            """)
    Page<RiskScoreResponseDTO> findByPetId(@Param("petId") Long petId, Pageable pageable);
    
    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.RiskScoreResponseDTO(
                rs.id, rs.score, rs.calculatedAt,
                rs.pet.id, rs.pet.name,
                rs.riskLevel.id, rs.riskLevel.name
            )
            FROM RiskScore rs
            WHERE rs.pet.id = :petId
            ORDER BY rs.calculatedAt DESC
            """)
    Page<RiskScoreResponseDTO> findLatestByPetId(@Param("petId") Long petId, Pageable pageable);
}