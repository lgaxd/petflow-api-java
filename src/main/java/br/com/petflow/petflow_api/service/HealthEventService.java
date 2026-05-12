package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.HealthEventRequestDTO;
import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.entity.Clinic;
import br.com.petflow.petflow_api.entity.HealthEvent;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.enums.HealthEventStatus;
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

    @Transactional
    @CacheEvict(value = "healthEvents", allEntries = true)
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

        HealthEvent healthEvent = HealthEvent.builder()
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .status(status)
                .pet(pet)
                .clinic(clinic)
                .build();

        healthEvent = healthEventRepository.save(healthEvent);
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
        
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            HealthEventStatus newStatus;
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

        if (request.getClinicId() != null) {
            Clinic clinic = clinicRepository.findById(request.getClinicId())
                    .orElseThrow(() -> new EntityNotFoundException("Clínica", request.getClinicId()));
            healthEvent.setClinic(clinic);
        }

        healthEvent = healthEventRepository.save(healthEvent);
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

    private void validateStatusTransition(HealthEventStatus currentStatus, HealthEventStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case AGENDADO -> newStatus == HealthEventStatus.REALIZADO || newStatus == HealthEventStatus.CANCELADO;
            case REALIZADO, CANCELADO -> false;
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException(
                    "HealthEvent", currentStatus.name(), newStatus.name());
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
                .clinicId(healthEvent.getClinic() != null ? healthEvent.getClinic().getId() : null)
                .clinicName(healthEvent.getClinic() != null ? healthEvent.getClinic().getName() : null)
                .build();
    }
}