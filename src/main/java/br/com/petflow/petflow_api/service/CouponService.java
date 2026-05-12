package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.CouponRequestDTO;
import br.com.petflow.petflow_api.dto.CouponResponseDTO;
import br.com.petflow.petflow_api.entity.Coupon;
import br.com.petflow.petflow_api.enums.CouponStatus;
import br.com.petflow.petflow_api.exception.BusinessRuleException;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponseDTO create(CouponRequestDTO request) {
        Optional<Coupon> existing = couponRepository.findByCode(request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Cupom", "código", request.getCode());
        }

        CouponStatus status = CouponStatus.DISPONIVEL;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = CouponStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: DISPONIVEL, RESGATADO, UTILIZADO");
            }
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .status(status)
                .expirationDate(request.getExpirationDate())
                .build();

        coupon = couponRepository.save(coupon);
        return toResponseDTO(coupon);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "coupons", key = "#id")
    public CouponResponseDTO findById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", id));
        return toResponseDTO(coupon);
    }

    @Cacheable(value = "coupons", key = "#status + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CouponResponseDTO> findAll(String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            try {
                CouponStatus couponStatus = CouponStatus.valueOf(status.toUpperCase());
                return couponRepository.findByStatus(couponStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: DISPONIVEL, RESGATADO, UTILIZADO");
            }
        }
        return couponRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponseDTO updateStatus(Long id, String newStatusStr) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", id));

        CouponStatus newStatus;
        try {
            newStatus = CouponStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido. Valores permitidos: DISPONIVEL, RESGATADO, UTILIZADO");
        }

        validateStatusTransition(coupon.getStatus(), newStatus);
        coupon.setStatus(newStatus);

        coupon = couponRepository.save(coupon);
        return toResponseDTO(coupon);
    }

    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new EntityNotFoundException("Cupom", id);
        }
        couponRepository.deleteById(id);
    }

    private void validateStatusTransition(CouponStatus currentStatus, CouponStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus != CouponStatus.DISPONIVEL) {
            throw new BusinessRuleException(
                    String.format("Cupom com status '%s' não pode ser alterado para '%s'", 
                            currentStatus.name(), newStatus.name()),
                    "INVALID_COUPON_STATUS_TRANSITION"
            );
        }

        if (newStatus != CouponStatus.UTILIZADO && newStatus != CouponStatus.RESGATADO) {
            throw new BusinessRuleException(
                    String.format("Status inválido para cupom: %s. Transições permitidas: %s, %s", 
                            newStatus.name(), CouponStatus.UTILIZADO.name(), CouponStatus.RESGATADO.name()),
                    "INVALID_COUPON_STATUS_TRANSITION"
            );
        }
    }

    private CouponResponseDTO toResponseDTO(Coupon coupon) {
        return CouponResponseDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .status(coupon.getStatus())
                .expirationDate(coupon.getExpirationDate())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}