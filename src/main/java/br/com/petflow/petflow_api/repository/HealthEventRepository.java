package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.entity.HealthEvent;
import br.com.petflow.petflow_api.enums.HealthEventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HealthEventRepository extends JpaRepository<HealthEvent, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.HealthEventResponseDTO(
                h.id, h.description, h.eventDate, h.status, h.createdAt,
                h.pet.id, h.pet.name, h.clinic.id, h.clinic.name
            )
            FROM HealthEvent h
            WHERE h.pet.id = :petId
            """)
    Page<HealthEventResponseDTO> findByPetIdProjected(@Param("petId") Long petId, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.HealthEventResponseDTO(
                h.id, h.description, h.eventDate, h.status, h.createdAt,
                h.pet.id, h.pet.name, h.clinic.id, h.clinic.name
            )
            FROM HealthEvent h
            WHERE h.status = :status
            """)
    Page<HealthEventResponseDTO> findByStatusProjected(@Param("status") HealthEventStatus status, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.HealthEventResponseDTO(
                h.id, h.description, h.eventDate, h.status, h.createdAt,
                h.pet.id, h.pet.name, h.clinic.id, h.clinic.name
            )
            FROM HealthEvent h
            """)
    Page<HealthEventResponseDTO> findAllProjected(Pageable pageable);
}