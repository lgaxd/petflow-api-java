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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public List<RiskScoreResponseDTO> findAll() {
        return riskScoreRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<RiskScoreResponseDTO> findByPetId(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return riskScoreRepository.findByPetId(petId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RiskScoreResponseDTO getLatestByPetId(Long petId) {
        List<RiskScore> scores = riskScoreRepository.findByPetId(petId);
        if (scores.isEmpty()) {
            throw new EntityNotFoundException("Score de Risco para o Pet", petId);
        }
        RiskScore latest = scores.stream()
                .max((a, b) -> a.getCalculatedAt().compareTo(b.getCalculatedAt()))
                .orElseThrow();
        return toResponseDTO(latest);
    }

    @Transactional
    @CacheEvict(value = "riskScores", allEntries = true)
    public void delete(Long id) {
        if (!riskScoreRepository.existsById(id)) {
            throw new EntityNotFoundException("Score de Risco", id);
        }
        riskScoreRepository.deleteById(id);
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