package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.HealthEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthEventRepository extends JpaRepository<HealthEvent, Long> {

    Page<HealthEvent> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<HealthEvent> findByPetId(Long petId, Pageable pageable);
}