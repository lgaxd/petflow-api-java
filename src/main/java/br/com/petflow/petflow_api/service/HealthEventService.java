package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.HealthEventRequestDTO;
import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.entity.*;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.InvalidStatusTransitionException;
import br.com.petflow.petflow_api.repository.*;
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
    private final EventTypeRepository eventTypeRepository;
    private final ClinicRepository clinicRepository;
    private final RewardPointService rewardPointService;

    private static final String STATUS_AGENDADO = "AGENDADO";
    private static final String STATUS_REALIZADO = "REALIZADO";
    private static final String STATUS_CANCELADO = "CANCELADO";

    @Transactional
    @CacheEvict(value = "healthEvents", allEntries = true)
    public HealthEventResponseDTO create(HealthEventRequestDTO request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.getPetId()));

        EventType eventType = eventTypeRepository.findById(request.getEventTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Evento", request.getEventTypeId()));

        Clinic clinic = null;
        if (request.getClinicId() != null) {
            clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
        }

        HealthEvent healthEvent = HealthEvent.builder()
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .status(request.getStatus())
                .pet(pet)
                .eventType(eventType)
                .clinic(clinic)
                .build();

        healthEvent = healthEventRepository.save(healthEvent);

        // Se o evento já for REALIZADO, gera pontos de recompensa
        if (STATUS_REALIZADO.equals(request.getStatus())) {
            rewardPointService.generatePointsFromHealthEvent(healthEvent);
        }

        return toResponseDTO(healthEvent);
    }

    @Cacheable(value = "healthEvents", key = "#id")
    public HealthEventResponseDTO findById(Long id) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));
        return toResponseDTO(healthEvent);
    }

    public Page<HealthEventResponseDTO> findAll(Pageable pageable) {
        return healthEventRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<HealthEventResponseDTO> findByStatus(String status, Pageable pageable) {
        return healthEventRepository.findByStatusIgnoreCase(status, pageable)
                .map(this::toResponseDTO);
    }

    public Page<HealthEventResponseDTO> findByPetId(Long petId, Pageable pageable) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return healthEventRepository.findByPetId(petId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "healthEvents", key = "#id")
    public HealthEventResponseDTO updateStatus(Long id, String newStatus) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));

        validateStatusTransition(healthEvent.getStatus(), newStatus);

        String oldStatus = healthEvent.getStatus();
        healthEvent.setStatus(newStatus);
        healthEvent = healthEventRepository.save(healthEvent);

        // Se mudou de AGENDADO para REALIZADO, gera pontos
        if (STATUS_AGENDADO.equals(oldStatus) && STATUS_REALIZADO.equals(newStatus)) {
            rewardPointService.generatePointsFromHealthEvent(healthEvent);
        }

        return toResponseDTO(healthEvent);
    }

    @Transactional
    @CacheEvict(value = "healthEvents", key = "#id")
    public void delete(Long id) {
        if (!healthEventRepository.existsById(id)) {
            throw new EntityNotFoundException("Evento de Saúde", id);
        }
        healthEventRepository.deleteById(id);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case STATUS_AGENDADO -> 
                STATUS_REALIZADO.equals(newStatus) || STATUS_CANCELADO.equals(newStatus);
            case STATUS_REALIZADO -> false;
            case STATUS_CANCELADO -> false;
            default -> false;
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException("HealthEvent", currentStatus, newStatus);
        }
    }

    private HealthEventResponseDTO toResponseDTO(HealthEvent healthEvent) {
        return HealthEventResponseDTO.builder()
                .id(healthEvent.getId())
                .description(healthEvent.getDescription())
                .eventDate(healthEvent.getEventDate())
                .status(healthEvent.getStatus())
                .createdAt(healthEvent.getCreatedAt())
                .petId(healthEvent.getPet().getId())
                .petName(healthEvent.getPet().getName())
                .eventTypeId(healthEvent.getEventType().getId())
                .eventTypeName(healthEvent.getEventType().getName())
                .clinicId(healthEvent.getClinic() != null ? healthEvent.getClinic().getId() : null)
                .clinicName(healthEvent.getClinic() != null ? healthEvent.getClinic().getName() : null)
                .build();
    }
}