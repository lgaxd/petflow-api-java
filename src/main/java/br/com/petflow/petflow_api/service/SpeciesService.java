package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.SpeciesRequestDTO;
import br.com.petflow.petflow_api.dto.SpeciesResponseDTO;
import br.com.petflow.petflow_api.entity.Species;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.SpeciesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpeciesService {

    private final SpeciesRepository speciesRepository;

    @Transactional
    @CacheEvict(value = "species", allEntries = true)
    public SpeciesResponseDTO create(SpeciesRequestDTO request) {
        Optional<Species> existing = speciesRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Espécie", "nome", request.getName());
        }

        Species species = Species.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        species = speciesRepository.save(species);
        return toResponseDTO(species);
    }

    @Cacheable(value = "species", key = "#id")
    public SpeciesResponseDTO findById(Long id) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Espécie", id));
        return toResponseDTO(species);
    }

    public Page<SpeciesResponseDTO> findAll(Pageable pageable) {
        return speciesRepository.findAll(pageable).map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "species", allEntries = true)
    public SpeciesResponseDTO update(Long id, SpeciesRequestDTO request) {
        Species species = speciesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Espécie", id));

        Optional<Species> existing = speciesRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DuplicateResourceException("Espécie", "nome", request.getName());
        }

        species.setName(request.getName());
        species.setDescription(request.getDescription());

        species = speciesRepository.save(species);
        return toResponseDTO(species);
    }

    @Transactional
    @CacheEvict(value = "species", allEntries = true)
    public void delete(Long id) {
        if (!speciesRepository.existsById(id)) {
            throw new EntityNotFoundException("Espécie", id);
        }
        speciesRepository.deleteById(id);
    }

    private SpeciesResponseDTO toResponseDTO(Species species) {
        return SpeciesResponseDTO.builder()
                .id(species.getId())
                .name(species.getName())
                .description(species.getDescription())
                .build();
    }
}