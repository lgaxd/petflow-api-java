package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.HealthEventRequestDTO;
import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.entity.Clinic;
import br.com.petflow.petflow_api.entity.HealthEvent;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.enums.EventType;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.InvalidStatusTransitionException;
import br.com.petflow.petflow_api.repository.ClinicRepository;
import br.com.petflow.petflow_api.repository.HealthEventRepository;
import br.com.petflow.petflow_api.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthEventService {

    private final HealthEventRepository healthEventRepository;
    private final PetRepository petRepository;
    private final ClinicRepository clinicRepository;

    private static final String STATUS_AGENDADO = "AGENDADO";
    private static final String STATUS_REALIZADO = "REALIZADO";
    private static final String STATUS_CANCELADO = "CANCELADO";

    @Transactional
    @CacheEvict(value = "healthEvents", allEntries = true)
    public HealthEventResponseDTO create(HealthEventRequestDTO request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.getPetId()));

        EventType eventType;
        try {
            eventType = EventType.valueOf(request.getEventType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de evento inválido. Valores permitidos: VACCINE, EXAM, CONSULTATION, SURGERY");
        }

        Clinic clinic = null;
        if (request.getClinicId() != null) {
            clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
        }

        HealthEvent healthEvent = HealthEvent.builder()
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .status(request.getStatus() != null ? request.getStatus() : STATUS_AGENDADO)
                .pet(pet)
                .eventType(eventType)
                .clinic(clinic)
                .build();

        healthEvent = healthEventRepository.save(healthEvent);
        return toResponseDTO(healthEvent);
    }

    @Cacheable(value = "healthEvents", key = "#id")
    public HealthEventResponseDTO findById(Long id) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));
        return toResponseDTO(healthEvent);
    }

    public Page<HealthEventResponseDTO> findAll(Long petId, String eventType, String status, Pageable pageable) {
        Page<HealthEvent> page = healthEventRepository.findAll(pageable);
        if (petId != null) {
            page = healthEventRepository.findByPetId(petId, pageable);
        } else if (status != null) {
            page = healthEventRepository.findByStatusIgnoreCase(status, pageable);
        }
        return page.map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "healthEvents", key = "#id")
    public HealthEventResponseDTO update(Long id, HealthEventRequestDTO request) {
        HealthEvent healthEvent =