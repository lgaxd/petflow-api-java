package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.HealthEventRequestDTO;
import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.entity.*;
import br.com.petflow.petflow_api.enums.HealthEventStatus;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.InvalidStatusTransitionException;
import br.com.petflow.petflow_api.repository.*;
import br.com.petflow.petflow_api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    private final GamificationProcedureService gamificationProcedureService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "healthEvents", allEntries = true),
            @CacheEvict(value = "tutorPoints", allEntries = true)
    })
    public HealthEventResponseDTO create(HealthEventRequestDTO request) {
        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.petId()));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(pet.getTutor().getId());
        }

        HealthEventStatus status = HealthEventStatus.AGENDADO;
        if (request.status() != null && !request.status().isBlank()) {
            try {
                status = HealthEventStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: AGENDADO, REALIZADO, CANCELADO");
            }
        }

        Clinic clinic = null;
        if (request.clinicId() != null) {
            clinic = clinicRepository.findById(request.clinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.clinicId()));
        }

        EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                .orElseThrow(() -> new EntityNotFoundException("EventType", request.eventTypeId()));

        HealthEvent healthEvent = HealthEvent.builder()
                .description(request.description())
                .eventDate(request.eventDate())
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
    public HealthEventResponseDTO findById(Long id) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(healthEvent.getPet().getTutor().getId());
        }

        return toResponseDTO(healthEvent);
    }

    @Transactional(readOnly = true)
    public Page<HealthEventResponseDTO> findAll(Long petId, String status, Pageable pageable) {
        HealthEventStatus healthStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                healthStatus = HealthEventStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: AGENDADO, REALIZADO, CANCELADO");
            }
        }

        Long currentTutorId = SecurityUtils.isAdmin() ? null : SecurityUtils.getCurrentTutorId();
        if (petId != null) {
            if (currentTutorId != null) {
                Pet pet = petRepository.findById(petId)
                        .orElseThrow(() -> new EntityNotFoundException("Pet", petId));
                if (!currentTutorId.equals(pet.getTutor().getId())) {
                    throw new AccessDeniedException("Você não tem permissão para visualizar eventos de outro tutor.");
                }
            }
            return healthStatus == null
                    ? healthEventRepository.findByPetIdProjected(petId, pageable)
                    : healthEventRepository.findByPetIdAndStatusProjected(petId, healthStatus, pageable);
        }
        if (currentTutorId != null) {
            return healthStatus == null
                    ? healthEventRepository.findByTutorIdProjected(currentTutorId, pageable)
                    : healthEventRepository.findByTutorIdAndStatusProjected(currentTutorId, healthStatus, pageable);
        }
        if (healthStatus != null) {
            return healthEventRepository.findByStatusProjected(healthStatus, pageable);
        }
        return healthEventRepository.findAllProjected(pageable);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "healthEvents", allEntries = true),
            @CacheEvict(value = "tutorPoints", allEntries = true)
    })
    public HealthEventResponseDTO update(Long id, HealthEventRequestDTO request) {
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(healthEvent.getPet().getTutor().getId());
        }

        HealthEventStatus oldStatus = healthEvent.getStatus();
        HealthEventStatus newStatus = oldStatus;

        if (request.status() != null && !request.status().isBlank()) {
            try {
                newStatus = HealthEventStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: AGENDADO, REALIZADO, CANCELADO");
            }
            if (oldStatus != newStatus) {
                validateStatusTransition(oldStatus, newStatus);
                healthEvent.setStatus(newStatus);
            }
        }

        healthEvent.setDescription(request.description());
        healthEvent.setEventDate(request.eventDate());

        if (request.eventTypeId() != null) {
            EventType eventType = eventTypeRepository.findById(request.eventTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("EventType", request.eventTypeId()));
            healthEvent.setEventType(eventType);
        }

        if (request.clinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.clinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.clinicId()));
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
        HealthEvent healthEvent = healthEventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Evento de Saúde", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(healthEvent.getPet().getTutor().getId());
        }

        healthEventRepository.delete(healthEvent);
    }

    private void grantPointsForEvent(Tutor tutor, HealthEvent event) {
        gamificationProcedureService.grantPoints(
                tutor.getId(), "EVENTO_SAUDE_REALIZADO", "HEALTH_EVENT", event.getId());
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