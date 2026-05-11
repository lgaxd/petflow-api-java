package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.PlanRequestDTO;
import br.com.petflow.petflow_api.dto.PlanResponseDTO;
import br.com.petflow.petflow_api.entity.Clinic;
import br.com.petflow.petflow_api.entity.Plan;
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
public class PlanService {

    private final PlanRepository planRepository;
    private final ClinicRepository clinicRepository;

    @Transactional
    @CacheEvict(value = "plans", allEntries = true)
    public PlanResponseDTO create(PlanRequestDTO request) {
        Clinic clinic = clinicRepository.findById(request.getClinicId())
                .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));

        Plan plan = Plan.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .pointsPerEvent(request.getPointsPerEvent())
                .clinic(clinic)
                .build();

        plan = planRepository.save(plan);
        return toResponseDTO(plan);
    }

    @Cacheable(value = "plans", key = "#id")
    public PlanResponseDTO findById(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plano", id));
        return toResponseDTO(plan);
    }

    public Page<PlanResponseDTO> findAll(Pageable pageable) {
        return planRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<PlanResponseDTO> findAll(Long clinicId, Pageable pageable) {
        if (clinicId != null) {
            return planRepository.findByClinicId(clinicId, pageable)
                    .map(this::toResponseDTO);
        }
        return planRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<PlanResponseDTO> findByName(String name, Pageable pageable) {
        return planRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponseDTO);
    }

    public Page<PlanResponseDTO> findByClinicId(Long clinicId, Pageable pageable) {
        if (!clinicRepository.existsById(clinicId)) {
            throw new EntityNotFoundException("Clínica", clinicId);
        }
        return planRepository.findByClinicId(clinicId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "plans", key = "#id")
    public PlanResponseDTO update(Long id, PlanRequestDTO request) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plano", id));

        if (!plan.getClinic().getId().equals(request.getClinicId())) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
            plan.setClinic(clinic);
        }

        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        plan.setPointsPerEvent(request.getPointsPerEvent());

        plan = planRepository.save(plan);
        return toResponseDTO(plan);
    }

    @Transactional
    @CacheEvict(value = "plans", key = "#id")
    public void delete(Long id) {
        if (!planRepository.existsById(id)) {
            throw new EntityNotFoundException("Plano", id);
        }
        planRepository.deleteById(id);
    }

    private PlanResponseDTO toResponseDTO(Plan plan) {
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