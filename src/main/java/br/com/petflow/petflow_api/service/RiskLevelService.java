package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.RiskLevelRequestDTO;
import br.com.petflow.petflow_api.dto.RiskLevelResponseDTO;
import br.com.petflow.petflow_api.entity.RiskLevel;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.RiskLevelRepository;
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
public class RiskLevelService {

    private final RiskLevelRepository riskLevelRepository;

    @Transactional
    @CacheEvict(value = "riskLevels", allEntries = true)
    public RiskLevelResponseDTO create(RiskLevelRequestDTO request) {
        Optional<RiskLevel> existing = riskLevelRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent()) {
            throw new DuplicateResourceException("Nível de Risco", "nome", request.getName());
        }

        validateScoreRange(request.getMinScore(), request.getMaxScore());

        RiskLevel riskLevel = RiskLevel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .minScore(request.getMinScore())
                .maxScore(request.getMaxScore())
                .build();

        riskLevel = riskLevelRepository.save(riskLevel);
        return toResponseDTO(riskLevel);
    }

    @Cacheable(value = "riskLevels", key = "#id")
    public RiskLevelResponseDTO findById(Long id) {
        RiskLevel riskLevel = riskLevelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nível de Risco", id));
        return toResponseDTO(riskLevel);
    }

    public Page<RiskLevelResponseDTO> findAll(Pageable pageable) {
        return riskLevelRepository.findAllProjected(pageable);
    }

    public RiskLevelResponseDTO findByName(String name) {
        RiskLevel riskLevel = riskLevelRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("Nível de Risco", "nome", name));
        return toResponseDTO(riskLevel);
    }

    public RiskLevel findByScore(Integer score) {
        return riskLevelRepository.findAll().stream()
                .filter(rl -> score >= rl.getMinScore() && score <= rl.getMaxScore())
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Nível de Risco", "score", String.valueOf(score)));
    }

    @Transactional
    @CacheEvict(value = "riskLevels", allEntries = true)
    public RiskLevelResponseDTO update(Long id, RiskLevelRequestDTO request) {
        RiskLevel riskLevel = riskLevelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nível de Risco", id));

        Optional<RiskLevel> existing = riskLevelRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DuplicateResourceException("Nível de Risco", "nome", request.getName());
        }

        validateScoreRange(request.getMinScore(), request.getMaxScore());

        riskLevel.setName(request.getName());
        riskLevel.setDescription(request.getDescription());
        riskLevel.setMinScore(request.getMinScore());
        riskLevel.setMaxScore(request.getMaxScore());

        riskLevel = riskLevelRepository.save(riskLevel);
        return toResponseDTO(riskLevel);
    }

    @Transactional
    @CacheEvict(value = "riskLevels", allEntries = true)
    public void delete(Long id) {
        if (!riskLevelRepository.existsById(id)) {
            throw new EntityNotFoundException("Nível de Risco", id);
        }
        riskLevelRepository.deleteById(id);
    }

    private void validateScoreRange(Integer minScore, Integer maxScore) {
        if (minScore < 0) {
            throw new IllegalArgumentException("Score mínimo não pode ser negativo");
        }
        if (maxScore <= minScore) {
            throw new IllegalArgumentException("Score máximo deve ser maior que o mínimo");
        }
    }

    private RiskLevelResponseDTO toResponseDTO(RiskLevel riskLevel) {
        return RiskLevelResponseDTO.builder()
                .id(riskLevel.getId())
                .name(riskLevel.getName())
                .description(riskLevel.getDescription())
                .minScore(riskLevel.getMinScore())
                .maxScore(riskLevel.getMaxScore())
                .build();
    }
}