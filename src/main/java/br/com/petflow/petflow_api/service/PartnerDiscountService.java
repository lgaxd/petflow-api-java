package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.PartnerDiscountRequestDTO;
import br.com.petflow.petflow_api.dto.PartnerDiscountResponseDTO;
import br.com.petflow.petflow_api.entity.Clinic;
import br.com.petflow.petflow_api.entity.PartnerDiscount;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.ClinicRepository;
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
public class PartnerDiscountService {

    private final PartnerDiscountRepository partnerDiscountRepository;
    private final ClinicRepository clinicRepository;

    @Transactional
    @CacheEvict(value = "partnerDiscounts", allEntries = true)
    public PartnerDiscountResponseDTO create(PartnerDiscountRequestDTO request) {
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));

        PartnerDiscount discount = PartnerDiscount.builder()
                .partnerName(request.getPartnerName())
                .category(request.getCategory())
                .discountPercent(request.getDiscountPercent())
                .clinic(clinic)
                .build();

        discount = partnerDiscountRepository.save(discount);
        return toResponseDTO(discount);
    }

    @Cacheable(value = "partnerDiscounts", key = "#id")
    public PartnerDiscountResponseDTO findById(Long id) {
        PartnerDiscount discount = partnerDiscountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Desconto de Parceiro", id));
        return toResponseDTO(discount);
    }

    public Page<PartnerDiscountResponseDTO> findAll(Pageable pageable) {
        return partnerDiscountRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<PartnerDiscountResponseDTO> findByCategory(String category, Pageable pageable) {
        return partnerDiscountRepository.findByCategoryIgnoreCase(category, pageable);
    }

    public Page<PartnerDiscountResponseDTO> findByClinicId(Long clinicId, Pageable pageable) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new EntityNotFoundException("Clínica", clinicId);
        }
        return partnerDiscountRepository.findByClinicId(clinicId, pageable);
    }

    @Transactional
    @CacheEvict(value = "partnerDiscounts", allEntries = true)
    public PartnerDiscountResponseDTO update(Long id, PartnerDiscountRequestDTO request) {
        PartnerDiscount discount = partnerDiscountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Desconto de Parceiro", id));

        if (!discount.getClinic().getId().equals(request.getClinicId())) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
            discount.setClinic(clinic);
        }

        discount.setPartnerName(request.getPartnerName());
        discount.setCategory(request.getCategory());
        discount.setDiscountPercent(request.getDiscountPercent());

        discount = partnerDiscountRepository.save(discount);
        return toResponseDTO(discount);
    }

    @Transactional
    @CacheEvict(value = "partnerDiscounts", allEntries = true)
    public void delete(Long id) {
        if (!partnerDiscountRepository.existsById(id)) {
            throw new EntityNotFoundException("Desconto de Parceiro", id);
        }
        partnerDiscountRepository.deleteById(id);
    }

    private PartnerDiscountResponseDTO toResponseDTO(PartnerDiscount discount) {
        return PartnerDiscountResponseDTO.builder()
                .id(discount.getId())
                .partnerName(discount.getPartnerName())
                .category(discount.getCategory())
                .discountPercent(discount.getDiscountPercent())
                .clinicId(discount.getClinic().getId())
                .clinicName(discount.getClinic().getName())
                .build();
    }
}