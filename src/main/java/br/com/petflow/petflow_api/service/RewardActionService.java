package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.RewardActionRequestDTO;
import br.com.petflow.petflow_api.dto.RewardActionResponseDTO;
import br.com.petflow.petflow_api.entity.RewardAction;
import br.com.petflow.petflow_api.exception.DuplicateResourceException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.RewardActionRepository;
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
public class RewardActionService {

    private final RewardActionRepository rewardActionRepository;

    @Transactional
    @CacheEvict(value = "rewardActions", allEntries = true)
    public RewardActionResponseDTO create(RewardActionRequestDTO request) {
        if (rewardActionRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Ação de Recompensa", "nome", request.getName());
        }

        RewardAction rewardAction = RewardAction.builder()
                .name(request.getName())
                .pointsValue(request.getPointsValue())
                .description(request.getDescription())
                .build();

        rewardAction = rewardActionRepository.save(rewardAction);
        return toResponseDTO(rewardAction);
    }

    @Cacheable(value = "rewardActions", key = "#id")
    public RewardActionResponseDTO findById(Long id) {
        RewardAction rewardAction = rewardActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ação de Recompensa", id));
        return toResponseDTO(rewardAction);
    }

    public Page<RewardActionResponseDTO> findAll(Pageable pageable) {
        return rewardActionRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "rewardActions", allEntries = true)
    public RewardActionResponseDTO update(Long id, RewardActionRequestDTO request) {
        RewardAction rewardAction = rewardActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ação de Recompensa", id));

        Optional<RewardAction> existing = rewardActionRepository.findByNameIgnoreCase(request.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DuplicateResourceException("Ação de Recompensa", "nome", request.getName());
        }

        rewardAction.setName(request.getName());
        rewardAction.setPointsValue(request.getPointsValue());
        rewardAction.setDescription(request.getDescription());

        rewardAction = rewardActionRepository.save(rewardAction);
        return toResponseDTO(rewardAction);
    }

    @Transactional
    @CacheEvict(value = "rewardActions", allEntries = true)
    public void delete(Long id) {
        if (!rewardActionRepository.existsById(id)) {
            throw new EntityNotFoundException("Ação de Recompensa", id);
        }
        rewardActionRepository.deleteById(id);
    }

    private RewardActionResponseDTO toResponseDTO(RewardAction rewardAction) {
        return RewardActionResponseDTO.builder()
                .id(rewardAction.getId())
                .name(rewardAction.getName())
                .pointsValue(rewardAction.getPointsValue())
                .description(rewardAction.getDescription())
                .build();
    }
}