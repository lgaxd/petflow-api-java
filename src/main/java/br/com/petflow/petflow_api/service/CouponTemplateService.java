package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.CouponTemplateRequestDTO;
import br.com.petflow.petflow_api.dto.CouponTemplateResponseDTO;
import br.com.petflow.petflow_api.entity.CouponTemplate;
import br.com.petflow.petflow_api.entity.PartnerDiscount;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.CouponTemplateRepository;
import br.com.petflow.petflow_api.repository.PartnerDiscountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponTemplateService {

    private final CouponTemplateRepository couponTemplateRepository;
    private final PartnerDiscountRepository partnerDiscountRepository;

    @Transactional
    @CacheEvict(value = "couponTemplates", allEntries = true)
    public CouponTemplateResponseDTO create(CouponTemplateRequestDTO request) {
        PartnerDiscount partnerDiscount = partnerDiscountRepository.findById(request.getPartnerDiscountId())
                .orElseThrow(() -> new EntityNotFoundException("Desconto de Parceiro", request.getPartnerDiscountId()));

        CouponTemplate template = CouponTemplate.builder()
                .title(request.getTitle())
                .discountValue(request.getDiscountValue())
                .discountType(request.getDiscountType())
                .pointsRequired(request.getPointsRequired())
                .partnerDiscount(partnerDiscount)
                .build();

        template = couponTemplateRepository.save(template);
        return toResponseDTO(template);
    }

    @Cacheable(value = "couponTemplates", key = "#id")
    public CouponTemplateResponseDTO findById(Long id) {
        CouponTemplate template = couponTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template de Cupom", id));
        return toResponseDTO(template);
    }

    public Page<CouponTemplateResponseDTO> findAll(Pageable pageable) {
        return couponTemplateRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "couponTemplates", allEntries = true)
    public CouponTemplateResponseDTO update(Long id, CouponTemplateRequestDTO request) {
        CouponTemplate template = couponTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Template de Cupom", id));

        if (!template.getPartnerDiscount().getId().equals(request.getPartnerDiscountId())) {
            PartnerDiscount partnerDiscount = partnerDiscountRepository.findById(request.getPartnerDiscountId())
                    .orElseThrow(() -> new EntityNotFoundException("Desconto de Parceiro", request.getPartnerDiscountId()));
            template.setPartnerDiscount(partnerDiscount);
        }

        template.setTitle(request.getTitle());
        template.setDiscountValue(request.getDiscountValue());
        template.setDiscountType(request.getDiscountType());
        template.setPointsRequired(request.getPointsRequired());

        template = couponTemplateRepository.save(template);
        return toResponseDTO(template);
    }

    @Transactional
    @CacheEvict(value = "couponTemplates", allEntries = true)
    public void delete(Long id) {
        if (!couponTemplateRepository.existsById(id)) {
            throw new EntityNotFoundException("Template de Cupom", id);
        }
        couponTemplateRepository.deleteById(id);
    }

    private CouponTemplateResponseDTO toResponseDTO(CouponTemplate template) {
        return CouponTemplateResponseDTO.builder()
                .id(template.getId())
                .title(template.getTitle())
                .discountValue(template.getDiscountValue())
                .discountType(template.getDiscountType())
                .pointsRequired(template.getPointsRequired())
                .partnerDiscountId(template.getPartnerDiscount().getId())
                .partnerName(template.getPartnerDiscount().getPartnerName())
                .build();
    }
}