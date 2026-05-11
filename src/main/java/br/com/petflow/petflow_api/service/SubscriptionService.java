package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.SubscriptionRequestDTO;
import br.com.petflow.petflow_api.dto.SubscriptionResponseDTO;
import br.com.petflow.petflow_api.entity.Pet;
import br.com.petflow.petflow_api.entity.Plan;
import br.com.petflow.petflow_api.entity.Subscription;
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

    private static final String STATUS_ATIVO = "ATIVO";
    private static final String STATUS_ENCERRADO = "ENCERRADO";
    private static final String STATUS_CANCELADO = "CANCELADO";
    private static final String STATUS_EXPIRADO = "EXPIRADO";

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

        Subscription subscription = Subscription.builder()
                .startDate(request.getStartDate())
                .endDate(endDate)
                .status(request.getStatus() != null ? request.getStatus() : STATUS_ATIVO)
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

    public Page<SubscriptionResponseDTO> findAll(Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<SubscriptionResponseDTO> findAll(Long petId, Long planId, String status, Pageable pageable) {
        if (petId != null) {
            return subscriptionRepository.findByPetId(petId, pageable).map(this::toResponseDTO);
        } else if (status != null) {
            return subscriptionRepository.findByStatusIgnoreCase(status, pageable).map(this::toResponseDTO);
        }
        return subscriptionRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<SubscriptionResponseDTO> findByStatus(String status, Pageable pageable) {
        return subscriptionRepository.findByStatusIgnoreCase(status, pageable)
                .map(this::toResponseDTO);
    }

    public Page<SubscriptionResponseDTO> findByPetId(Long petId, Pageable pageable) {
        if (!petRepository.existsById(petId)) {
            throw new EntityNotFoundException("Pet", petId);
        }
        return subscriptionRepository.findByPetId(petId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional
    @CacheEvict(value = "subscriptions", key = "#id")
    public SubscriptionResponseDTO updateStatus(Long id, String newStatus) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

        validateStatusTransition(subscription.getStatus(), newStatus);

        subscription.setStatus(newStatus);
        subscription = subscriptionRepository.save(subscription);
        return toResponseDTO(subscription);
    }

    @Transactional
    @CacheEvict(value = "subscriptions", key = "#id")
    public SubscriptionResponseDTO cancel(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Assinatura", id));

        if (!STATUS_ATIVO.equals(subscription.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Subscription", subscription.getStatus(), STATUS_CANCELADO);
        }

        subscription.setStatus(STATUS_CANCELADO);
        subscription = subscriptionRepository.save(subscription);
        return toResponseDTO(subscription);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    @CacheEvict(value = "subscriptions", allEntries = true)
    public void expireSubscriptions() {
        List<Subscription> activeSubscriptions = subscriptionRepository
                .findByStatusIgnoreCase(STATUS_ATIVO, Pageable.unpaged()).getContent();

        LocalDate today = LocalDate.now();
        for (Subscription subscription : activeSubscriptions) {
            if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(today)) {
                subscription.setStatus(STATUS_EXPIRADO);
                subscriptionRepository.save(subscription);
            }
        }
    }

    @Transactional
    @CacheEvict(value = "subscriptions", key = "#id")
    public void delete(Long id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new EntityNotFoundException("Assinatura", id);
        }
        subscriptionRepository.deleteById(id);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (currentStatus.equals(newStatus)) {
            return;
        }

        if (STATUS_ATIVO.equals(currentStatus)) {
            if (!STATUS_ENCERRADO.equals(newStatus) && !STATUS_CANCELADO.equals(newStatus)) {
                throw new InvalidStatusTransitionException("Subscription", currentStatus, newStatus);
            }
        } else {
            throw new InvalidStatusTransitionException("Subscription", currentStatus, newStatus);
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