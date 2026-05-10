package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.RewardActionResponseDTO;
import br.com.petflow.petflow_api.entity.RewardAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RewardActionRepository extends JpaRepository<RewardAction, Long> {

    Optional<RewardAction> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
    
    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.RewardActionResponseDTO(
                ra.id, ra.name, ra.pointsValue, ra.description
            )
            FROM RewardAction ra
            """)
    Page<RewardActionResponseDTO> findAllProjected(Pageable pageable);
}