package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.SubscriptionRequestDTO;
import br.com.petflow.petflow_api.dto.SubscriptionResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.Plan;
import br.com.petflow.petflow_api.entity.Subscription;
import br.com.petflow.petflow_api.enums.SubscriptionStatus;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.InvalidStatusTransitionException;
import br.com.petflow.petflow_api.repository.PetRepository;
import br.com.petflow.petflow_api.repository.PlanRepository;
import br.com.petflow.petflow_api.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public SubscriptionResponseDTO create(SubscriptionRequestDTO request) {
        Pet pet = petRepository.findById(request.getPetId())
                .orElseThrow(() -> new EntityNotFoundException("Pet", request.getPetId()));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Plano", request.getPlanId()));

        LocalDate endDate = request.getEndDate();
        if (endDate == null && request.getStartDate() != null) {
            endDate = request.getStartDate().plusDays(plan.getDurationDays());
        }

        SubscriptionStatus status = SubscriptionStatus.ATIVO;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = SubscriptionStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido. Valores permitidos: ATIVO, ENCERRADO, CANCELADO, EXPIRADO");
            }
        }

        Subscription subscription = Subscription.builder()
                .startDate(request.getStartDate())
                .endDate(endDate)
                .status(status)
                .pet(pet)
                .plan(plan)
                .build();

        subscription = subscriptionRepository.save(subscription);
        return toResponseDTO(subscription);
    }

    @Cacheable(value = "subscriptions", key = "#id")
    public SubscriptionResponseDTO findById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));
        return toResponseDTO(subscription);
    }

    @Cacheable(value = "subscriptions", key = "#petId + '_' + #status + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<SubscriptionResponseDTO> findAll(Long petId, String status, Pageable pageable) {
        if (petId != null) {
            return subscriptionRepository.findByPetIdProjected(petId, pageable);
        } else if (status != null && !status.isBlank()) {
            return subscriptionRepository.findByStatusProjected(status.toUpperCase(), pageable);
        }
        return subscriptionRepository.findAllProjected(pageable);
    }

    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public SubscriptionResponseDTO updateStatus(Long id, String newStatusStr) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

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
        Page<SubscriptionResponseDTO> activePage = subscriptionRepository
                .findByStatusProjected(SubscriptionStatus.ATIVO.name(), Pageable.unpaged());

        LocalDate today = LocalDate.now();
        for (SubscriptionResponseDTO dto : activePage.getContent()) {
            Subscription subscription = subscriptionRepository.findById(dto.getId()).orElse(null);
            if (subscription != null && subscription.getEndDate() != null && subscription.getEndDate().isBefore(today)) {
                subscription.setStatus(SubscriptionStatus.EXPIRADO);
                subscriptionRepository.save(subscription);
            }
        }
    }

    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public void delete(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new EntityNotFoundException("Assinatura", id);
        }
        subscriptionRepository.deleteById(id);
    }

    private void validateStatusTransition(SubscriptionStatus currentStatus, SubscriptionStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus == SubscriptionStatus.ATIVO) {
            if (newStatus != SubscriptionStatus.ENCERRADO && newStatus != SubscriptionStatus.CANCELADO) {
                throw new InvalidStatusTransitionException(
                        "Subscription", currentStatus.name(), newStatus.name());
            }
        } else {
            throw new InvalidStatusTransitionException(
                    "Subscription", currentStatus.name(), newStatus.name());
        }
    }

    private SubscriptionResponseDTO toResponseDTO(Subscription subscription) {
        return SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .status(subscription.getStatus().name())
                .createdAt(subscription.getCreatedAt())
                .petId(subscription.getPet().getId())
                .petName(subscription.getPet().getName())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getName())
                .build();
    }
}