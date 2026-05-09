package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Page<Subscription> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<Subscription> findByPetId(Long petId, Pageable pageable);
}