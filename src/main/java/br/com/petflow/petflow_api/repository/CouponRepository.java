package br.com.petflow.petflow_api.repository;

import br.com.petflow.petflow_api.dto.CouponResponseDTO;
import br.com.petflow.petflow_api.entity.Coupon;
import br.com.petflow.petflow_api.enums.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.CouponResponseDTO(
                c.id, c.code, c.status, c.expirationDate, c.createdAt
            )
            FROM Coupon c
            """)
    Page<CouponResponseDTO> findAllProjected(Pageable pageable);

    @Query("""
            SELECT new br.com.petflow.petflow_api.dto.CouponResponseDTO(
                c.id, c.code, c.status, c.expirationDate, c.createdAt
            )
            FROM Coupon c
            WHERE c.status = :status
            """)
    Page<CouponResponseDTO> findByStatus(@Param("status") CouponStatus status, Pageable pageable);
}