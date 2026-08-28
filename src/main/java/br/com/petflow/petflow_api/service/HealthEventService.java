package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.HealthEventRequestDTO;
import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.entity.*;
import br.com.petflow.petflow_api.enums.HealthEventStatus;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.InvalidStatusTransitionException;
import br.com.petflow.petflow_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final EventTypeRepository eventTypeRepository;
    private final RewardActionRepository rewardActionRepository;
    private final RewardPointRepository rewardPointRepository;

    @Transactional
        @Caching(evict = {
            @CacheEvict(value = "healthEvents", allEntries = true),
            @CacheEvict(value = "tutorPoints", allEntries = true)
        })
    public HealthEventResponseDTO create(HealthEventRequestDTO request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.getPetId()));

        HealthEventStatus status = HealthEventStatus.AGENDADO;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = HealthEventStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: AGENDADO, REALIZADO, CANCELADO");
            }
        }

        Clinic clinic = null;
        if (request.getClinicId() != null) {
            clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
        }

        EventType eventType = eventTypeRepository.findById(request.getEventTypeId())
                .orElseThrow(() -> new EntityNotFoundException("EventType", request.getEventTypeId()));

        HealthEvent healthEvent = HealthEvent.builder()
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .status(status)
                .eventType(eventType)
                .pet(pet)
                .clinic(clinic)
                .build();

        healthEvent = healthEventRepository.save(healthEvent);

        // Se o evento já for criado como REALIZADO, conceder pontos
        if (status == HealthEventStatus.REALIZADO) {
            grantPointsForEvent(pet.getTutor(), healthEvent);
        }

        return toResponseDTO(healthEvent);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "healthEvents", key = "#id")
    public HealthEventResponseDTO findById(Long id) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));
        return toResponseDTO(healthEvent);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "healthEvents", key = "#petId + '_' + #status + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<HealthEventResponseDTO> findAll(Long petId, String status, Pageable pageable) {
        if (petId != null) {
            return healthEventRepository.findByPetIdProjected(petId, pageable);
        } else if (status != null && !status.isBlank()) {
            try {
                HealthEventStatus healthStatus = HealthEventStatus.valueOf(status.toUpperCase());
                return healthEventRepository.findByStatusProjected(healthStatus, pageable);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: AGENDADO, REALIZADO, CANCELADO");
            }
        }
        return healthEventRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "healthEvents", allEntries = true)
    public HealthEventResponseDTO update(Long id, HealthEventRequestDTO request) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));

        HealthEventStatus oldStatus = healthEvent.getStatus();
        HealthEventStatus newStatus = oldStatus;

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                newStatus = HealthEventStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: AGENDADO, REALIZADO, CANCELADO");
            }
            if (oldStatus != newStatus) {
                validateStatusTransition(oldStatus, newStatus);
                healthEvent.setStatus(newStatus);
            }
        }

        healthEvent.setDescription(request.getDescription());
        healthEvent.setEventDate(request.getEventDate());

        if (request.getEventTypeId() != null) {
            EventType eventType = eventTypeRepository.findById(request.getEventTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("EventType", request.getEventTypeId()));
            healthEvent.setEventType(eventType);
        }

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
            healthEvent.setClinic(clinic);
        }

        healthEvent = healthEventRepository.save(healthEvent);

        // Se o status mudou para REALIZADO, conceder pontos (evitar duplicidade)
        if (oldStatus != HealthEventStatus.REALIZADO && newStatus == HealthEventStatus.REALIZADO) {
            grantPointsForEvent(healthEvent.getPet().getTutor(), healthEvent);
        }

        return toResponseDTO(healthEvent);
    }

    @Transactional
    @CacheEvict(value = "healthEvents", allEntries = true)
    public void delete(Long id) {
        if (!healthEventRepository.existsById(id)) {
            throw new EntityNotFoundException("Evento de Saúde", id);
        }
        healthEventRepository.deleteById(id);
    }

    private void grantPointsForEvent(Tutor tutor, HealthEvent event) {
        RewardAction action = rewardActionRepository.findByName("EVENTO_SAUDE_REALIZADO")
                .orElseThrow(() -> new EntityNotFoundException("RewardAction", "name", "EVENTO_SAUDE_REALIZADO"));
        RewardPoint rewardPoint = RewardPoint.builder()
                .tutor(tutor)
                .rewardAction(action)
                .points(action.getPointsValue())
                .referenceType("HEALTH_EVENT")
                .referenceId(event.getId())
                .build();
        rewardPointRepository.save(rewardPoint);
    }

    private void validateStatusTransition(HealthEventStatus currentStatus, HealthEventStatus newStatus) {
        if (currentStatus == newStatus) return;
        boolean isValid = switch (currentStatus) {
            case AGENDADO -> newStatus == HealthEventStatus.REALIZADO || newStatus == HealthEventStatus.CANCELADO;
            case REALIZADO, CANCELADO -> false;
        };
        if (!isValid) {
            throw new InvalidStatusTransitionException("HealthEvent", currentStatus.name(), newStatus.name());
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
                .eventTypeId(healthEvent.getEventType() != null ? healthEvent.getEventType().getId() : null)
                .clinicId(healthEvent.getClinic() != null ? healthEvent.getClinic().getId() : null)
                .clinicName(healthEvent.getClinic() != null ? healthEvent.getClinic().getName() : null)
                .build();
    }
}