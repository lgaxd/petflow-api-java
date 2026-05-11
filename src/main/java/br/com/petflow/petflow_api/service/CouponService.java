package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.CouponRequestDTO;
import br.com.petflow.petflow_api.dto.CouponResponseDTO;
import br.com.petflow.petflow_api.entity.Coupon;
import br.com.petflow.petflow_api.entity.CouponTemplate;
import br.com.petflow.petflow_api.exception.BusinessRuleException;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.CouponRepository;
import br.com.petflow.petflow_api.repository.CouponTemplateRepository;
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
    private final CouponTemplateRepository templateRepository;

    private static final String STATUS_DISPONIVEL = "DISPONIVEL";
    private static final String STATUS_UTILIZADO = "UTILIZADO";
    private static final String STATUS_RESGATADO = "RESGATADO";

    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponseDTO create(CouponRequestDTO request) {
        Optional<Coupon> existing = couponRepository.findByCode(request.getCode());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Cupom", "código", request.getCode());
        }

        CouponTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new EntityNotFoundException("Template de Cupom", request.getTemplateId()));

        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .status(request.getStatus() != null ? request.getStatus() : STATUS_DISPONIVEL)
                .expirationDate(request.getExpirationDate())
                .template(template)
                .build();

        coupon = couponRepository.save(coupon);
        return toResponseDTO(coupon);
    }

    @Cacheable(value = "coupons", key = "#id")
    public CouponResponseDTO findById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", id));
        return toResponseDTO(coupon);
    }

    public CouponResponseDTO findByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", "código", code));
        return toResponseDTO(coupon);
    }

    public Page<CouponResponseDTO> findAll(Pageable pageable) {
        return couponRepository.findAllProjected(pageable);
    }

    public Page<CouponResponseDTO> findAll(Long templateId, String status, Pageable pageable) {
        if (status != null) {
            return couponRepository.findByStatusIgnoreCase(status, pageable);
        }
        return couponRepository.findAllProjected(pageable);
    }

    public Page<CouponResponseDTO> findByStatus(String status, Pageable pageable) {
        return couponRepository.findByStatusIgnoreCase(status, pageable);
    }

    @Transactional
    @CacheEvict(value = "coupons", key = "#id")
    public CouponResponseDTO updateStatus(Long id, String newStatus) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", id));

        validateStatusTransition(coupon.getStatus(), newStatus);
        coupon.setStatus(newStatus);

        coupon = couponRepository.save(coupon);
        return toResponseDTO(coupon);
    }

    @Transactional
    @CacheEvict(value = "coupons", key = "#id")
    public CouponResponseDTO update(Long id, CouponRequestDTO request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", id));

        Optional<Coupon> existing = couponRepository.findByCode(request.getCode());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DuplicateResourceException("Cupom", "código", request.getCode());
        }

        if (!coupon.getTemplate().getId().equals(request.getTemplateId())) {
            CouponTemplate template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("Template de Cupom", request.getTemplateId()));
            coupon.setTemplate(template);
        }

        coupon.setCode(request.getCode());
        coupon.setStatus(request.getStatus());
        coupon.setExpirationDate(request.getExpirationDate());

        coupon = couponRepository.save(coupon);
        return toResponseDTO(coupon);
    }

    @Transactional
    @CacheEvict(value = "coupons", key = "#id")
    public void delete(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new EntityNotFoundException("Cupom", id);
        }
        couponRepository.deleteById(id);
    }

    /**
     * Valida a transição de status do cupom
     * Regras:
     * - DISPONIVEL → UTILIZADO ou DISPONIVEL → RESGATADO (permitido)
     * - Qualquer outro status não pode ser alterado
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        // Apenas cupons DISPONIVEL podem mudar de status
        if (!STATUS_DISPONIVEL.equals(currentStatus)) {
            throw new BusinessRuleException(
                    String.format("Cupom com status '%s' não pode ser alterado para '%s'", 
                            currentStatus, newStatus),
                    "INVALID_COUPON_STATUS_TRANSITION"
            );
        }

        // Apenas transições para UTILIZADO ou RESGATADO são permitidas
        if (!STATUS_UTILIZADO.equals(newStatus) && !STATUS_RESGATADO.equals(newStatus)) {
            throw new BusinessRuleException(
                    String.format("Status inválido para cupom: %s. Transições permitidas: %s, %s", 
                            newStatus, STATUS_UTILIZADO, STATUS_RESGATADO),
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
                .templateId(coupon.getTemplate().getId())
                .templateTitle(coupon.getTemplate().getTitle())
                .build();
    }
}