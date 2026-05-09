package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Page<Plan> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Plan> findByClinicId(Long clinicId, Pageable pageable);
}