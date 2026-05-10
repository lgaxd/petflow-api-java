package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Page<Subscription> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<Subscription> findByPetId(Long petId, Pageable pageable);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ATIVO' AND s.endDate < :today")
    List<Subscription> findExpiredSubscriptions(@Param("today") LocalDate today);
}