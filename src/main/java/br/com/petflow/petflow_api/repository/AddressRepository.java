package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.AddressResponseDTO;
import br.com.petflow.petflow_api.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.AddressResponseDTO(
                a.id, a.street, a.city, a.state, a.zipCode, a.tutor.id
            )
            FROM Address a
            WHERE LOWER(a.city) = LOWER(:city)
            """)
    Page<AddressResponseDTO> findByCityIgnoreCase(@Param("city") String city, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.AddressResponseDTO(
                a.id, a.street, a.city, a.state, a.zipCode, a.tutor.id
            )
            FROM Address a
            WHERE LOWER(a.state) = LOWER(:state)
            """)
    Page<AddressResponseDTO> findByStateIgnoreCase(@Param("state") String state, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.AddressResponseDTO(
                a.id, a.street, a.city, a.state, a.zipCode, a.tutor.id
            )
            FROM Address a
            WHERE a.tutor.id = :tutorId
            """)
    Page<AddressResponseDTO> findByTutorId(@Param("tutorId") Long tutorId, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.AddressResponseDTO(
                a.id, a.street, a.city, a.state, a.zipCode, a.tutor.id
            )
            FROM Address a
            WHERE a.id = :id
            """)
    java.util.Optional<AddressResponseDTO> findProjectedById(@Param("id") Long id);
}