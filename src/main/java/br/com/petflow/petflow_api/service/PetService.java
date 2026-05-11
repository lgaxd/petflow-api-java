package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.*;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.Species;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
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
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final SpeciesRepository speciesRepository;
    private final HealthEventRepository healthEventRepository;
    private final RiskScoreRepository riskScoreRepository;
    private final SubscriptionRepository subscriptionRepository;

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

    public Page<PetResponseDTO> findAll(Long tutorId, Long speciesId, Pageable pageable) {
        if (tutorId != null && speciesId != null) {
            Page<Pet> pets = petRepository.findByTutorId(tutorId, pageable);
            return pets.map(this::toResponseDTO);
        } else if (tutorId != null) {
            return petRepository.findByTutorId(tutorId, pageable).map(this::toResponseDTO);
        } else if (speciesId != null) {
            return petRepository.findBySpeciesId(speciesId, pageable).map(this::toResponseDTO);
        }
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
        return petRepository.findByTutorId(tutorId, pageable).map(this::toResponseDTO);
    }

    public Page<PetResponseDTO> findBySpeciesId(Long speciesId, Pageable pageable) {
        if (!speciesRepository.existsById(speciesId)) {
            throw new EntityNotFoundException("Espécie", speciesId);
        }
        return petRepository.findBySpeciesId(speciesId, pageable).map(this::toResponseDTO);
    }

    public Page<HealthEventResponseDTO> findHealthEventsByPetId(Long petId, Pageable pageable) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return healthEventRepository.findByPetId(petId, pageable).map(this::toHealthEventResponseDTO);
    }

    public Page<RiskScoreResponseDTO> findRiskScoresByPetId(Long petId, Pageable pageable) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return riskScoreRepository.findByPetId(petId, pageable);
    }

    public Page<SubscriptionResponseDTO> findSubscriptionsByPetId(Long petId, Pageable pageable) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return subscriptionRepository.findByPetId(petId, pageable).map(this::toSubscriptionResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "pets", key = "#id")
    public PetResponseDTO update(Long id, PetRequestDTO request) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));

        if (request.getTutorId() != null && !pet.getTutor().getId().equals(request.getTutorId())) {
            Tutor tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));
            pet.setTutor(tutor);
        }

        if (request.getSpeciesId() != null && !pet.getSpecies().getId().equals(request.getSpeciesId())) {
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

    private HealthEventResponseDTO toHealthEventResponseDTO(br.com.petflow.petflow_api.entity.HealthEvent he) {
        return HealthEventResponseDTO.builder()
                .id(he.getId())
                .description(he.getDescription())
                .eventDate(he.getEventDate())
                .status(he.getStatus())
                .createdAt(he.getCreatedAt())
                .petId(he.getPet().getId())
                .petName(he.getPet().getName())
                .eventTypeId(he.getEventType().getId())
                .eventTypeName(he.getEventType().getName())
                .clinicId(he.getClinic() != null ? he.getClinic().getId() : null)
                .clinicName(he.getClinic() != null ? he.getClinic().getName() : null)
                .build();
    }

    private SubscriptionResponseDTO toSubscriptionResponseDTO(br.com.petflow.petflow_api.entity.Subscription sub) {
        return SubscriptionResponseDTO.builder()
                .id(sub.getId())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .status(sub.getStatus())
                .createdAt(sub.getCreatedAt())
                .petId(sub.getPet().getId())
                .petName(sub.getPet().getName())
                .planId(sub.getPlan().getId())
                .planName(sub.getPlan().getName())
                .build();
    }
}