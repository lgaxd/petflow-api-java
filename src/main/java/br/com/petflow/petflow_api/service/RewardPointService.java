package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.RewardPointRequestDTO;
import br.com.petflow.petflow_api.dto.RewardPointResponseDTO;
import br.com.petflow.petflow_api.entity.*;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.repository.RewardActionRepository;
import br.com.petflow.petflow_api.repository.RewardPointRepository;
import br.com.petflow.petflow_api.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class RewardPointService {
 
    private final RewardPointRepository rewardPointRepository;
    private final TutorRepository tutorRepository;
    private final RewardActionRepository rewardActionRepository;
 
    private static final String HEALTH_EVENT_ACTION_NAME = "EVENTO_DE_SAUDE";
 
    @Transactional
    @CacheEvict(value = "rewardPoints", allEntries = true)
    public RewardPointResponseDTO create(RewardPointRequestDTO request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));
 
        RewardAction rewardAction = rewardActionRepository.findById(request.getRewardActionId())
                .orElseThrow(() -> new EntityNotFoundException("Ação de Recompensa", request.getRewardActionId()));
 
        RewardPoint rewardPoint = RewardPoint.builder()
                .points(request.getPoints())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .tutor(tutor)
                .rewardAction(rewardAction)
                .build();
 
        rewardPoint = rewardPointRepository.save(rewardPoint);
        return toResponseDTO(rewardPoint);
    }
 
    @Transactional
    public void generatePointsFromHealthEvent(HealthEvent healthEvent) {
        Integer points = healthEvent.getEventType().getPointsReward();
 
        Pet pet = healthEvent.getPet();
 
        if (pet.getSubscriptions() != null) {
            Subscription activeSubscription = pet.getSubscriptions().stream()
                    .filter(s -> "ATIVO".equals(s.getStatus()))
                    .findFirst()
                    .orElse(null);
 
            if (activeSubscription != null) {
                Integer bonus = activeSubscription.getPlan().getPointsPerEvent();
                if (bonus != null && bonus > 0) {
                    points += bonus;
                }
            }
        }
 
        // CORRIGIDO: busca por nome ao invés de ID fixo (1L)
        RewardAction rewardAction = rewardActionRepository
                .findByNameIgnoreCase(HEALTH_EVENT_ACTION_NAME)
                .orElseThrow(() -> new EntityNotFoundException(
                        "RewardAction", "name", HEALTH_EVENT_ACTION_NAME));
 
        RewardPoint rewardPoint = RewardPoint.builder()
                .points(points)
                .referenceType("HEALTH_EVENT")
                .referenceId(healthEvent.getId())
                .tutor(pet.getTutor())
                .rewardAction(rewardAction)
                .build();
 
        rewardPointRepository.save(rewardPoint);
    }
 
    @Cacheable(value = "rewardPoints", key = "#id")
    public RewardPointResponseDTO findById(Long id) {
        RewardPoint rewardPoint = rewardPointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ponto de Recompensa", id));
        return toResponseDTO(rewardPoint);
    }
 
    // CORRIGIDO: paginado para não retornar lista inteira sem controle
    public Page<RewardPointResponseDTO> findByTutorId(Long tutorId, Pageable pageable) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return rewardPointRepository.findByTutorId(tutorId, pageable)
                .map(this::toResponseDTO);
    }
 
    public Integer getTotalPointsByTutor(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        // Soma direto no banco — evita carregar todos os registros na memória
        return rewardPointRepository.sumPointsByTutorId(tutorId);
    }
 
    @Transactional
    @CacheEvict(value = "rewardPoints", key = "#id")
    public void delete(Long id) {
        if (!rewardPointRepository.existsById(id)) {
            throw new EntityNotFoundException("Ponto de Recompensa", id);
        }
        rewardPointRepository.deleteById(id);
    }
 
    private RewardPointResponseDTO toResponseDTO(RewardPoint rewardPoint) {
        return RewardPointResponseDTO.builder()
                .id(rewardPoint.getId())
                .points(rewardPoint.getPoints())
                .referenceType(rewardPoint.getReferenceType())
                .referenceId(rewardPoint.getReferenceId())
                .createdAt(rewardPoint.getCreatedAt())
                .tutorId(rewardPoint.getTutor().getId())
                .tutorName(rewardPoint.getTutor().getName())
                .rewardActionId(rewardPoint.getRewardAction().getId())
                .rewardActionName(rewardPoint.getRewardAction().getName())
                .build();
    }
}