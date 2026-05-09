package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByEmail(String email);

    Page<Tutor> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByEmail(String email);
}