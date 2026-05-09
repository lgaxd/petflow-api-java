package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.PetRequestDTO;
import br.com.petflow.petflow_api.dto.PetResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.Species;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.PetRepository;
import br.com.petflow.petflow_api.repository.SpeciesRepository;
import br.com.petflow.petflow_api.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final SpeciesRepository speciesRepository;

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetResponseDTO create(PetRequestDTO request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));

        Species species = speciesRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new EntityNotFoundException("Espécie", request.getSpeciesId()));

        Pet pet = Pet.builder()
                .name(request.getName())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .weight(request.getWeight())
                .tutor(tutor)
                .species(species)
                .build();

        pet = petRepository.save(pet);
        return toResponseDTO(pet);
    }

    @Cacheable(value = "pets", key = "#id")
    public PetResponseDTO findById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));
        return toResponseDTO(pet);
    }

    public Page<PetResponseDTO> findAll(Pageable pageable) {
        return petRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<PetResponseDTO> findByName(String name, Pageable pageable) {
        return petRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponseDTO);
    }

    public Page<PetResponseDTO> findByTutorId(Long tutorId, Pageable pageable) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return petRepository.findByTutorId(tutorId, pageable)
                .map(this::toResponseDTO);
    }

    public Page<PetResponseDTO> findBySpeciesId(Long speciesId, Pageable pageable) {
        if (!speciesRepository.existsById(speciesId)) {
            throw new EntityNotFoundException("Espécie", speciesId);
        }
        return petRepository.findBySpeciesId(speciesId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "pets", key = "#id")
    public PetResponseDTO update(Long id, PetRequestDTO request) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));

        if (!pet.getTutor().getId().equals(request.getTutorId())) {
            Tutor tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));
            pet.setTutor(tutor);
        }

        if (!pet.getSpecies().getId().equals(request.getSpeciesId())) {
            Species species = speciesRepository.findById(request.getSpeciesId())
                    .orElseThrow(() -> new EntityNotFoundException("Espécie", request.getSpeciesId()));
            pet.setSpecies(species);
        }

        pet.setName(request.getName());
        pet.setBreed(request.getBreed());
        pet.setBirthDate(request.getBirthDate());
        pet.setWeight(request.getWeight());

        pet = petRepository.save(pet);
        return toResponseDTO(pet);
    }

    @Transactional
    @CacheEvict(value = "pets", key = "#id")
    public void delete(Long id) {
        if (!petRepository.existsById(id)) {
            throw new EntityNotFoundException("Pet", id);
        }
        petRepository.deleteById(id);
    }

    private PetResponseDTO toResponseDTO(Pet pet) {
        return PetResponseDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .weight(pet.getWeight())
                .createdAt(pet.getCreatedAt())
                .tutorId(pet.getTutor().getId())
                .tutorName(pet.getTutor().getName())
                .speciesId(pet.getSpecies().getId())
                .speciesName(pet.getSpecies().getName())
                .build();
    }
}