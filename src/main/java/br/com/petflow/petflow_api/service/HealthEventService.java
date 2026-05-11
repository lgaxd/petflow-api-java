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

    public Page<HealthEventResponseDTO> findAll(Long petId, Long clinicId, String status, Long eventTypeId, Pageable pageable) {
        Page<HealthEvent> page;

        if (petId != null) {
            page = healthEventRepository.findByPetId(petId, pageable);
        } else if (status != null) {
            page = healthEventRepository.findByStatusIgnoreCase(status, pageable);
        } else {
            page = healthEventRepository.findAll(pageable);
        }

        return page.map(this::toResponseDTO);
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
    public HealthEventResponseDTO update(HealthEventRequestDTO request) {
        // Implementar se necessário
        throw new UnsupportedOperationException("Método ainda não implementado");
    }

    @Transactional
    @CacheEvict(value = "healthEvents", key = "#id")
    public HealthEventResponseDTO update(Long id, HealthEventRequestDTO request) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));

        String oldStatus = healthEvent.getStatus();
        String newStatus = request.getStatus();

        if (newStatus != null && !oldStatus.equals(newStatus)) {
            validateStatusTransition(oldStatus, newStatus);
            healthEvent.setStatus(newStatus);

            if (STATUS_AGENDADO.equals(oldStatus) && STATUS_REALIZADO.equals(newStatus)) {
                rewardPointService.generatePointsFromHealthEvent(healthEvent);
            }
        }

        healthEvent.setDescription(request.getDescription());
        healthEvent.setEventDate(request.getEventDate());

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
            healthEvent.setClinic(clinic);
        }

        healthEvent = healthEventRepository.save(healthEvent);
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