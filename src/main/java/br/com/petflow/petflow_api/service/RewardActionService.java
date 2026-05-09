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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardActionService {

    private final RewardActionRepository rewardActionRepository;

    @Transactional
    @CacheEvict(value = "rewardActions", allEntries = true)
    public RewardActionResponseDTO create(RewardActionRequestDTO request) {
        // Verificar se já existe uma ação com o mesmo nome
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

    @Cacheable(value = "rewardActions", key = "'all'")
    public List<RewardActionResponseDTO> findAll() {
        return rewardActionRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "rewardActions", allEntries = true)
    public RewardActionResponseDTO update(Long id, RewardActionRequestDTO request) {
        RewardAction rewardAction = rewardActionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ação de Recompensa", id));

        // Verificar se outro registro já possui o mesmo nome
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