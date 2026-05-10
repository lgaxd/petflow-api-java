package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.EventTypeRequestDTO;
import br.com.petflow.petflow_api.dto.EventTypeResponseDTO;
import br.com.petflow.petflow_api.entity.EventType;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.EventTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventTypeService {

    private final EventTypeRepository eventTypeRepository;

    @Transactional
    @CacheEvict(value = "eventTypes", allEntries = true)
    public EventTypeResponseDTO create(EventTypeRequestDTO request) {
        if (eventTypeRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Tipo de Evento", "nome", request.getName());
        }

        EventType eventType = EventType.builder()
                .name(request.getName())
                .pointsReward(request.getPointsReward())
                .category(request.getCategory())
                .build();

        eventType = eventTypeRepository.save(eventType);
        return toResponseDTO(eventType);
    }

    @Cacheable(value = "eventTypes", key = "#id")
    public EventTypeResponseDTO findById(Long id) {
        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Evento", id));
        return toResponseDTO(eventType);
    }

    public Page<EventTypeResponseDTO> findAll(Pageable pageable) {
        return eventTypeRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<EventTypeResponseDTO> findByCategory(String category, Pageable pageable) {
        return eventTypeRepository.findByCategoryIgnoreCase(category, pageable);
    }

    public Page<EventTypeResponseDTO> findByName(String name, Pageable pageable) {
        return eventTypeRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Transactional
    @CacheEvict(value = "eventTypes", allEntries = true)
    public EventTypeResponseDTO update(Long id, EventTypeRequestDTO request) {
        EventType eventType = eventTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Evento", id));

        if (eventTypeRepository.existsByNameIgnoreCase(request.getName()) &&
                !eventType.getName().equalsIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Tipo de Evento", "nome", request.getName());
        }

        eventType.setName(request.getName());
        eventType.setPointsReward(request.getPointsReward());
        eventType.setCategory(request.getCategory());

        eventType = eventTypeRepository.save(eventType);
        return toResponseDTO(eventType);
    }

    @Transactional
    @CacheEvict(value = "eventTypes", allEntries = true)
    public void delete(Long id) {
        if (!eventTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Tipo de Evento", id);
        }
        eventTypeRepository.deleteById(id);
    }

    private EventTypeResponseDTO toResponseDTO(EventType eventType) {
        return EventTypeResponseDTO.builder()
                .id(eventType.getId())
                .name(eventType.getName())
                .pointsReward(eventType.getPointsReward())
                .category(eventType.getCategory())
                .build();
    }
}