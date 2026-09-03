package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.PetRequestDTO;
import br.com.petflow.petflow_api.dto.PetResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.RewardAction;
import br.com.petflow.petflow_api.entity.RewardPoint;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.PetRepository;
import br.com.petflow.petflow_api.repository.RewardActionRepository;
import br.com.petflow.petflow_api.repository.RewardPointRepository;
import br.com.petflow.petflow_api.repository.TutorRepository;
import br.com.petflow.petflow_api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final RewardActionRepository rewardActionRepository;
    private final RewardPointRepository rewardPointRepository;

    @Transactional
        @Caching(evict = {
            @CacheEvict(value = "pets", allEntries = true),
            @CacheEvict(value = "tutorPoints", key = "#request.tutorId")
        })
    public PetResponseDTO create(PetRequestDTO request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));

        Pet pet = Pet.builder()
                .name(request.getName())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .weight(request.getWeight())
                .speciesId(request.getSpeciesId())
                .tutor(tutor)
                .build();

        pet = petRepository.save(pet);

        // Conceder pontos por cadastro de pet
        RewardAction action = rewardActionRepository.findByName("CADASTRO_PET")
                .orElseThrow(() -> new EntityNotFoundException("RewardAction", "name", "CADASTRO_PET"));
        RewardPoint rewardPoint = RewardPoint.builder()
                .tutor(tutor)
                .rewardAction(action)
                .points(action.getPointsValue())
                .referenceType("PET")
                .referenceId(pet.getId())
                .build();
        rewardPointRepository.save(rewardPoint);

        return toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "pets", key = "#id")
    public PetResponseDTO findById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(pet.getTutor().getId());
        }

        return toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "pets", key = "#name + '_' + #tutorId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<PetResponseDTO> findAll(String name, Long tutorId, Pageable pageable) {
        if (!SecurityUtils.isAdmin()) {
            Long currentTutorId = SecurityUtils.getCurrentTutorId();
            if (tutorId != null && !currentTutorId.equals(tutorId)) {
                throw new AccessDeniedException("Você não tem permissão para listar pets de outro tutor.");
            }
            tutorId = currentTutorId;
        }

        if (name != null && !name.isBlank()) {
            if (!SecurityUtils.isAdmin()) {
                Page<PetResponseDTO> pets = petRepository.findByTutorIdProjected(tutorId, pageable);
                List<PetResponseDTO> filtered = pets.getContent().stream()
                        .filter(pet -> pet.getName() != null && pet.getName().toLowerCase().contains(name.toLowerCase()))
                        .toList();
                return new PageImpl<>(filtered, pageable, filtered.size());
            }
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

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(pet.getTutor().getId());
        }

        if (request.getTutorId() != null && !pet.getTutor().getId().equals(request.getTutorId())) {
            if (!SecurityUtils.isAdmin()) {
                throw new AccessDeniedException("Você não pode transferir um pet para outro tutor.");
            }
            Tutor tutor = tutorRepository.findById(request.getTutorId())
                    .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));
            pet.setTutor(tutor);
        }

        pet.setName(request.getName());
        pet.setBreed(request.getBreed());
        pet.setBirthDate(request.getBirthDate());
        pet.setWeight(request.getWeight());
        pet.setSpeciesId(request.getSpeciesId());
        pet = petRepository.save(pet);
        return toResponseDTO(pet);
    }

    @Transactional
    @CacheEvict(value = "pets", allEntries = true)
    public void delete(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(pet.getTutor().getId());
        }

        petRepository.delete(pet);
    }

    private PetResponseDTO toResponseDTO(Pet pet) {
        return PetResponseDTO.builder()
                .id(pet.getId())
                .name(pet.getName())
                .breed(pet.getBreed())
                .birthDate(pet.getBirthDate())
                .weight(pet.getWeight())
                .speciesId(pet.getSpeciesId())
                .createdAt(pet.getCreatedAt())
                .tutorId(pet.getTutor().getId())
                .tutorName(pet.getTutor().getName())
                .build();
    }
}