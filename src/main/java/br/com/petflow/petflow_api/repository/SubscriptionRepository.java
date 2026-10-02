package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.SubscriptionResponseDTO;
import br.com.petflow.petflow_api.entity.Subscription;
import br.com.petflow.petflow_api.enums.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.SubscriptionResponseDTO(
                s.id, s.startDate, s.endDate, s.status, s.createdAt,
                s.pet.id, s.pet.name,
                s.plan.id, s.plan.name
            )
            FROM Subscription s
            WHERE s.pet.id = :petId
            """)
    Page<SubscriptionResponseDTO> findByPetIdProjected(@Param("petId") Long petId, Pageable pageable);

        @Query("""
            SELECT new br.com.petflow.petflow_api.dto.SubscriptionResponseDTO(
            s.id, s.startDate, s.endDate, s.status, s.createdAt,
            s.pet.id, s.pet.name, s.plan.id, s.plan.name
            )
            FROM Subscription s
            WHERE s.pet.id = :petId AND s.status = :status
            """)
        Page<SubscriptionResponseDTO> findByPetIdAndStatusProjected(
            @Param("petId") Long petId,
            @Param("status") SubscriptionStatus status,
            Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.SubscriptionResponseDTO(
                s.id, s.startDate, s.endDate, s.status, s.createdAt,
                s.pet.id, s.pet.name,
                s.plan.id, s.plan.name
            )
            FROM Subscription s
            WHERE s.status = :status
            """)
    Page<SubscriptionResponseDTO> findByStatusProjected(@Param("status") SubscriptionStatus status, Pageable pageable);

        @Query("""
            SELECT new br.com.petflow.petflow_api.dto.SubscriptionResponseDTO(
            s.id, s.startDate, s.endDate, s.status, s.createdAt,
            s.pet.id, s.pet.name, s.plan.id, s.plan.name
            )
            FROM Subscription s
            WHERE s.pet.tutor.id = :tutorId
            """)
        Page<SubscriptionResponseDTO> findByTutorIdProjected(@Param("tutorId") Long tutorId, Pageable pageable);

        @Query("""
            SELECT new br.com.petflow.petflow_api.dto.SubscriptionResponseDTO(
            s.id, s.startDate, s.endDate, s.status, s.createdAt,
            s.pet.id, s.pet.name, s.plan.id, s.plan.name
            )
            FROM Subscription s
            WHERE s.pet.tutor.id = :tutorId AND s.status = :status
            """)
        Page<SubscriptionResponseDTO> findByTutorIdAndStatusProjected(
            @Param("tutorId") Long tutorId,
            @Param("status") SubscriptionStatus status,
            Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.SubscriptionResponseDTO(
                s.id, s.startDate, s.endDate, s.status, s.createdAt,
                s.pet.id, s.pet.name,
                s.plan.id, s.plan.name
            )
            FROM Subscription s
            """)
    Page<SubscriptionResponseDTO> findAllProjected(Pageable pageable);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.endDate < :today")
    List<Subscription> findExpiredSubscriptions(@Param("status") SubscriptionStatus status, @Param("today") LocalDate today);
}