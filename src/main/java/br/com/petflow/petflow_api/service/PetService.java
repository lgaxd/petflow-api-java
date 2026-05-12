package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.PetRequestDTO;
import br.com.petflow.petflow_api.dto.PetResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.PetRepository;
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

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetResponseDTO create(PetRequestDTO request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));

        Pet pet = Pet.builder()
                .name(request.getName())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .weight(request.getWeight())
                .tutor(tutor)
                .build();

        pet = petRepository.save(pet);
        return toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "pets", key = "#id")
    public PetResponseDTO findById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));
        // O @Transactional mantém a sessão aberta, permitindo acesso aos dados lazy
        return toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "pets", key = "#name + '_' + #tutorId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PetResponseDTO> findAll(String name, Long tutorId, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return petRepository.findByNameProjected(name, pageable);
        } else if (tutorId != null) {
            return petRepository.findByTutorIdProjected(tutorId, pageable);
        }
        return petRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public PetResponseDTO update(Long id, PetRequestDTO request) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));

        if (request.getTutorId() != null && !pet.getTutor().getId().equals(request.getTutorId())) {
            Tutor tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));
            pet.setTutor(tutor);
        }

        pet.setName(request.getName());
        pet.setBreed(request.getBreed());
        pet.setBirthDate(request.getBirthDate());
        pet.setWeight(request.getWeight());

        pet = petRepository.save(pet);
        return toResponseDTO(pet);
    }

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
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
                .build();
    }
}