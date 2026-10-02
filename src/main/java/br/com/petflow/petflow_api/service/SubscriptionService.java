package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.SubscriptionRequestDTO;
import br.com.petflow.petflow_api.dto.SubscriptionResponseDTO;
import br.com.petflow.petflow_api.entity.*;
import br.com.petflow.petflow_api.enums.SubscriptionStatus;
import br.com.petflow.petflow_api.exception.BusinessRuleException;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.InvalidStatusTransitionException;
import br.com.petflow.petflow_api.repository.*;
import br.com.petflow.petflow_api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PetRepository petRepository;
    private final PlanRepository planRepository;
    private final GamificationProcedureService gamificationProcedureService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "subscriptions", allEntries = true),
            @CacheEvict(value = "tutorPoints", allEntries = true)
    })
    public SubscriptionResponseDTO create(SubscriptionRequestDTO request) {
        Pet pet = petRepository.findById(request.petId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.petId()));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(pet.getTutor().getId());
        }

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new EntityNotFoundException("Plano", request.planId()));

        LocalDate endDate = request.endDate();
        if (endDate == null && request.startDate() != null) {
            endDate = request.startDate().plusDays(plan.getDurationDays());
        }

        SubscriptionStatus status = SubscriptionStatus.ATIVO;
        if (request.status() != null && !request.status().isBlank()) {
            try {
                status = SubscriptionStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: ATIVO, ENCERRADO, CANCELADO, EXPIRADO");
            }
        }

        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new BusinessRuleException("A data de término não pode ser anterior à data de início", "INVALID_DATE_RANGE");
        }

        Subscription subscription = Subscription.builder()
                .startDate(request.startDate())
                .endDate(endDate)
                .status(status)
                .pet(pet)
                .plan(plan)
                .build();

        subscription = subscriptionRepository.save(subscription);

        // Se a assinatura for criada como ATIVO, conceder pontos
        if (status == SubscriptionStatus.ATIVO) {
            grantPointsForSubscription(pet.getTutor(), subscription);
        }

        return toResponseDTO(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO findById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(subscription.getPet().getTutor().getId());
        }

        return toResponseDTO(subscription);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponseDTO> findAll(Long petId, String status, Pageable pageable) {
        SubscriptionStatus subscriptionStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                subscriptionStatus = SubscriptionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: ATIVO, ENCERRADO, CANCELADO, EXPIRADO");
            }
        }

        Long currentTutorId = SecurityUtils.isAdmin() ? null : SecurityUtils.getCurrentTutorId();
        if (petId != null) {
            if (currentTutorId != null) {
                Pet pet = petRepository.findById(petId)
                        .orElseThrow(() -> new EntityNotFoundException("Pet", petId));
                if (!currentTutorId.equals(pet.getTutor().getId())) {
                    throw new AccessDeniedException("Você não tem permissão para visualizar assinaturas de outro tutor.");
                }
            }
            return subscriptionStatus == null
                    ? subscriptionRepository.findByPetIdProjected(petId, pageable)
                    : subscriptionRepository.findByPetIdAndStatusProjected(petId, subscriptionStatus, pageable);
        }
        if (currentTutorId != null) {
            return subscriptionStatus == null
                    ? subscriptionRepository.findByTutorIdProjected(currentTutorId, pageable)
                    : subscriptionRepository.findByTutorIdAndStatusProjected(currentTutorId, subscriptionStatus, pageable);
        }
        if (subscriptionStatus != null) {
            return subscriptionRepository.findByStatusProjected(subscriptionStatus, pageable);
        }
        return subscriptionRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public SubscriptionResponseDTO updateStatus(Long id, String newStatusStr) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(subscription.getPet().getTutor().getId());
        }

        SubscriptionStatus newStatus;
        try {
            newStatus = SubscriptionStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido. Valores permitidos: ATIVO, ENCERRADO, CANCELADO, EXPIRADO");
        }

        validateStatusTransition(subscription.getStatus(), newStatus);

        subscription.setStatus(newStatus);
        subscription = subscriptionRepository.save(subscription);
        return toResponseDTO(subscription);
    }

    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public SubscriptionResponseDTO cancel(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(subscription.getPet().getTutor().getId());
        }

        if (subscription.getStatus() != SubscriptionStatus.ATIVO) {
            throw new InvalidStatusTransitionException(
                    "Subscription", subscription.getStatus().name(), SubscriptionStatus.CANCELADO.name());
        }

        subscription.setStatus(SubscriptionStatus.CANCELADO);
        subscription = subscriptionRepository.save(subscription);
        return toResponseDTO(subscription);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public void expireSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredSubscriptions(SubscriptionStatus.ATIVO, LocalDate.now());

        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRADO);
            subscriptionRepository.save(subscription);
        }
    }

    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public void delete(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

        if (!SecurityUtils.isAdmin()) {
            SecurityUtils.checkOwnership(subscription.getPet().getTutor().getId());
        }

        subscriptionRepository.delete(subscription);
    }

    private void grantPointsForSubscription(Tutor tutor, Subscription subscription) {
        gamificationProcedureService.grantPoints(
                tutor.getId(), "ASSINATURA_ATIVA", "SUBSCRIPTION", subscription.getId());
    }

    private void validateStatusTransition(SubscriptionStatus currentStatus, SubscriptionStatus newStatus) {
        if (currentStatus == newStatus) return;
        if (currentStatus == SubscriptionStatus.ATIVO) {
            if (newStatus != SubscriptionStatus.ENCERRADO && newStatus != SubscriptionStatus.CANCELADO) {
                throw new InvalidStatusTransitionException("Subscription", currentStatus.name(), newStatus.name());
            }
        } else {
            throw new InvalidStatusTransitionException("Subscription", currentStatus.name(), newStatus.name());
        }
    }

    private SubscriptionResponseDTO toResponseDTO(Subscription subscription) {
        return SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus())
                .createdAt(subscription.getCreatedAt())
                .petId(subscription.getPet().getId())
                .petName(subscription.getPet().getName())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getName())
                .build();
    }
}