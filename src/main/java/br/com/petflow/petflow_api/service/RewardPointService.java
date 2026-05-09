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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RewardPointService {

    private final RewardPointRepository rewardPointRepository;
    private final TutorRepository tutorRepository;
    private final RewardActionRepository rewardActionRepository;

    // ID fixo para ação de recompensa de evento de saúde
    private static final Long HEALTH_EVENT_ACTION_ID = 1L;

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
        // Pontos base do tipo de evento
        Integer points = healthEvent.getEventType().getPointsReward();

        // Pontos adicionais do plano ativo do pet
        Pet pet = healthEvent.getPet();
        
        if (pet.getSubscriptions() != null) {
            // Buscar primeiro plano ativo sem modificar a variável points na lambda
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

        // Buscar ação de recompensa para evento de saúde
        RewardAction rewardAction = rewardActionRepository.findById(HEALTH_EVENT_ACTION_ID)
                .orElseThrow(() -> new EntityNotFoundException("Ação de Recompensa", HEALTH_EVENT_ACTION_ID));

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

    public List<RewardPointResponseDTO> findByTutorId(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return rewardPointRepository.findByTutorId(tutorId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Integer getTotalPointsByTutor(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return rewardPointRepository.findByTutorId(tutorId).stream()
                .mapToInt(RewardPoint::getPoints)
                .sum();
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