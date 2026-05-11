package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Pet> findByTutorId(Long tutorId, Pageable pageable);
}