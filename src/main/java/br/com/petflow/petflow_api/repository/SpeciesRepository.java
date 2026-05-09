package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Species;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpeciesRepository extends JpaRepository<Species, Long> {

    Optional<Species> findByNameIgnoreCase(String name);
}