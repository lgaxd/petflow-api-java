package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.EventTypeResponseDTO;
import br.com.petflow.petflow_api.entity.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.EventTypeResponseDTO(
                e.id, e.name, e.pointsReward, e.category
            )
            FROM EventType e
            WHERE LOWER(e.category) = LOWER(:category)
            """)
    Page<EventTypeResponseDTO> findByCategoryIgnoreCase(@Param("category") String category, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.EventTypeResponseDTO(
                e.id, e.name, e.pointsReward, e.category
            )
            FROM EventType e
            WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    Page<EventTypeResponseDTO> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    Optional<EventType> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}