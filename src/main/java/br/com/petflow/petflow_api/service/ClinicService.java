package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.ClinicRequestDTO;
import br.com.petflow.petflow_api.dto.ClinicResponseDTO;
import br.com.petflow.petflow_api.dto.PlanResponseDTO;
import br.com.petflow.petflow_api.entity.Clinic;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.ClinicRepository;
import br.com.petflow.petflow_api.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;
    private final PlanRepository planRepository;

    @Transactional
    @CacheEvict(value = "clinics", allEntries = true)
    public ClinicResponseDTO create(ClinicRequestDTO request) {
        if (clinicRepository.existsByCnpj(request.getCnpj())) {
            throw new DuplicateResourceException("Clínica", "CNPJ", request.getCnpj());
        }

        Clinic clinic = Clinic.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .cnpj(request.getCnpj())
                .build();

        clinic = clinicRepository.save(clinic);
        return toResponseDTO(clinic);
    }

    @Cacheable(value = "clinics", key = "#id")
    public ClinicResponseDTO findById(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", id));
        return toResponseDTO(clinic);
    }

    public Page<ClinicResponseDTO> findAll(Pageable pageable) {
        return clinicRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<ClinicResponseDTO> findByName(String name, Pageable pageable) {
        return clinicRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponseDTO);
    }

    public Page<PlanResponseDTO> findPlansByClinicId(Long clinicId, Pageable pageable) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new EntityNotFoundException("Clínica", clinicId);
        }
        return planRepository.findByClinicId(clinicId, pageable)
                .map(this::toPlanResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "clinics", key = "#id")
    public ClinicResponseDTO update(Long id, ClinicRequestDTO request) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Clínica", id));

        if (!clinic.getCnpj().equals(request.getCnpj()) &&
                clinicRepository.existsByCnpj(request.getCnpj())) {
            throw new DuplicateResourceException("Clínica", "CNPJ", request.getCnpj());
        }

        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        clinic.setPhone(request.getPhone());
        clinic.setCnpj(request.getCnpj());

        clinic = clinicRepository.save(clinic);
        return toResponseDTO(clinic);
    }

    @Transactional
    @CacheEvict(value = "clinics", key = "#id")
    public void delete(Long id) {
        if (!clinicRepository.existsById(id)) {
            throw new EntityNotFoundException("Clínica", id);
        }
        clinicRepository.deleteById(id);
    }

    private ClinicResponseDTO toResponseDTO(Clinic clinic) {
        return ClinicResponseDTO.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .phone(clinic.getPhone())
                .cnpj(clinic.getCnpj())
                .createdAt(clinic.getCreatedAt())
                .build();
    }

    private PlanResponseDTO toPlanResponseDTO(br.com.petflow.petflow_api.entity.Plan plan) {
        return PlanResponseDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .durationDays(plan.getDurationDays())
                .pointsPerEvent(plan.getPointsPerEvent())
                .clinicId(plan.getClinic().getId())
                .clinicName(plan.getClinic().getName())
                .build();
    }
}