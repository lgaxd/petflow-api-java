package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.PartnerDiscountResponseDTO;
import br.com.petflow.petflow_api.entity.PartnerDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartnerDiscountRepository extends JpaRepository<PartnerDiscount, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.PartnerDiscountResponseDTO(
                p.id, p.partnerName, p.category, p.discountPercent, p.clinic.id, p.clinic.name
            )
            FROM PartnerDiscount p
            WHERE LOWER(p.category) = LOWER(:category)
            """)
    Page<PartnerDiscountResponseDTO> findByCategoryIgnoreCase(@Param("category") String category, Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.PartnerDiscountResponseDTO(
                p.id, p.partnerName, p.category, p.discountPercent, p.clinic.id, p.clinic.name
            )
            FROM PartnerDiscount p
            WHERE p.clinic.id = :clinicId
            """)
    Page<PartnerDiscountResponseDTO> findByClinicId(@Param("clinicId") Long clinicId, Pageable pageable);
}