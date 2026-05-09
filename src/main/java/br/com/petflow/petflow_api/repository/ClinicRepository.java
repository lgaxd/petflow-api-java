package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Clinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    Page<Clinic> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByCnpj(String cnpj);
}