package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.PetResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetRepository extends JpaRepository<Pet, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.PetResponseDTO(
                p.id, p.name, p.breed, p.birthDate, p.weight, p.createdAt,
                p.tutor.id, p.tutor.name
            )
            FROM Pet p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    Page<PetResponseDTO> findByNameProjected(@Param("name") String name, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.PetResponseDTO(
                p.id, p.name, p.breed, p.birthDate, p.weight, p.createdAt,
                p.tutor.id, p.tutor.name
            )
            FROM Pet p
            WHERE p.tutor.id = :tutorId
            """)
    Page<PetResponseDTO> findByTutorIdProjected(@Param("tutorId") Long tutorId, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.PetResponseDTO(
                p.id, p.name, p.breed, p.birthDate, p.weight, p.createdAt,
                p.tutor.id, p.tutor.name
            )
            FROM Pet p
            """)
    Page<PetResponseDTO> findAllProjected(Pageable pageable);
}