package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.CouponTemplateResponseDTO;
import br.com.petflow.petflow_api.entity.CouponTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CouponTemplateRepository extends JpaRepository<CouponTemplate, Long> {

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.CouponTemplateResponseDTO(
                ct.id, ct.title, ct.discountValue, ct.discountType, 
                ct.pointsRequired, ct.partnerDiscount.id, ct.partnerDiscount.partnerName
            )
            FROM CouponTemplate ct
            """)
    Page<CouponTemplateResponseDTO> findAllProjected(Pageable pageable);
}