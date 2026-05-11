package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.RiskScoreRequestDTO;
import br.com.petflow.petflow_api.dto.RiskScoreResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.RiskLevel;
import br.com.petflow.petflow_api.entity.RiskScore;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.PetRepository;
import br.com.petflow.petflow_api.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskScoreService {

    private final RiskScoreRepository riskScoreRepository;
    private final PetRepository petRepository;
    private final RiskLevelService riskLevelService;

    @Transactional
    @CacheEvict(value = "riskScores", allEntries = true)
    public RiskScoreResponseDTO create(RiskScoreRequestDTO request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.getPetId()));

        RiskLevel riskLevel = riskLevelService.findByScore(request.getScore());

        RiskScore riskScore = RiskScore.builder()
                .score(request.getScore())
                .pet(pet)
                .riskLevel(riskLevel)
                .build();

        riskScore = riskScoreRepository.save(riskScore);
        return toResponseDTO(riskScore);
    }

    @Cacheable(value = "riskScores", key = "#id")
    public RiskScoreResponseDTO findById(Long id) {
        RiskScore riskScore = riskScoreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Score de Risco", id));
        return toResponseDTO(riskScore);
    }

    public Page<RiskScoreResponseDTO> findAll(Pageable pageable) {
        return riskScoreRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<RiskScoreResponseDTO> findAll(Long petId, Pageable pageable) {
        if (petId != null) {
            return riskScoreRepository.findByPetId(petId, pageable);
        }
        return riskScoreRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<RiskScoreResponseDTO> findByPetId(Long petId, Pageable pageable) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return riskScoreRepository.findByPetId(petId, pageable);
    }

    public RiskScoreResponseDTO getLatestByPetId(Long petId) {
        Page<RiskScoreResponseDTO> latestPage = riskScoreRepository.findLatestByPetId(petId, Pageable.ofSize(1));
        return latestPage.stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Score de Risco para o Pet", petId));
    }

    private RiskScoreResponseDTO toResponseDTO(RiskScore riskScore) {
        return RiskScoreResponseDTO.builder()
                .id(riskScore.getId())
                .score(riskScore.getScore())
                .calculatedAt(riskScore.getCalculatedAt())
                .petId(riskScore.getPet().getId())
                .petName(riskScore.getPet().getName())
                .riskLevelId(riskScore.getRiskLevel().getId())
                .riskLevelName(riskScore.getRiskLevel().getName())
                .build();
    }
}