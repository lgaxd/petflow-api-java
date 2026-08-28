package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.*;
import br.com.petflow.petflow_api.entity.*;
import br.com.petflow.petflow_api.enums.CouponStatus;
import br.com.petflow.petflow_api.exception.EntityNotFoundException;
import br.com.petflow.petflow_api.exception.BusinessRuleException;
import br.com.petflow.petflow_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final TutorRepository tutorRepository;
    private final RewardPointRepository rewardPointRepository;
    private final CouponTemplateRepository couponTemplateRepository;
    private final CouponRepository couponRepository;
    private final PetRepository petRepository;
    private final RewardActionRepository rewardActionRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "tutorPoints", key = "#tutorId")
    public TutorPointsDTO getTutorPoints(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("Tutor", tutorId));

        Integer totalPoints = rewardPointRepository.sumPointsByTutorId(tutorId);
        if (totalPoints == null) totalPoints = 0;

        Page<PointHistoryDTO> history = rewardPointRepository.findHistoryByTutorId(tutorId, Pageable.ofSize(50));

        return TutorPointsDTO.builder()
                .tutorId(tutor.getId())
                .tutorName(tutor.getName())
                .totalPoints(totalPoints)
                .history(history.getContent())
                .build();
    }

    @Transactional(readOnly = true)
    public PetRiskDTO getPetRisk(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new EntityNotFoundException("Pet", petId));

        // Cálculo simplificado do score de risco baseado em dados do pet
        Integer score = calculateRiskScore(pet);
        String level = getRiskLevel(score);
        String description = getRiskDescription(level);

        return PetRiskDTO.builder()
                .petId(pet.getId())
                .petName(pet.getName())
                .score(score)
                .riskLevel(level)
                .riskDescription(description)
                .build();
    }

    private Integer calculateRiskScore(Pet pet) {
        int score = 0;
        
        // Idade - pets mais velhos têm mais risco
        if (pet.getBirthDate() != null) {
            int age = LocalDate.now().getYear() - pet.getBirthDate().getYear();
            if (age > 10) score += 20;
            else if (age > 7) score += 10;
            else if (age > 5) score += 5;
        }
        
        // Peso - muito acima ou abaixo do ideal
        if (pet.getWeight() != null) {
            if (pet.getWeight() < 3.0 || pet.getWeight() > 30.0) score += 15;
            else if (pet.getWeight() < 5.0 || pet.getWeight() > 25.0) score += 8;
        }
        
        // Sempre entre 0 e 100
        return Math.min(score, 100);
    }

    private String getRiskLevel(Integer score) {
        if (score >= 61) return "ALTO";
        if (score >= 31) return "MEDIO";
        return "BAIXO";
    }

    private String getRiskDescription(String level) {
        return switch (level) {
            case "ALTO" -> "Requer acompanhamento veterinário próximo";
            case "MEDIO" -> "Atenção recomendada para cuidados preventivos";
            default -> "Pet saudável com baixo risco";
        };
    }

    @Transactional(readOnly = true)
    public Page<CouponCatalogDTO> getAvailableCoupons(Pageable pageable) {
        return couponTemplateRepository.findAvailableCoupons(pageable);
    }

    @Transactional
    @CacheEvict(value = "tutorPoints", key = "#tutorId")
    public RedeemResponseDTO redeemCoupon(Long tutorId, Long couponId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new EntityNotFoundException("Tutor", tutorId));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("Cupom", couponId));

        // Validar se o cupom está disponível
        if (coupon.getStatus() != CouponStatus.DISPONIVEL) {
            throw new BusinessRuleException("Cupom não está disponível para resgate");
        }

        // Validar se o cupom expirou
        if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Cupom expirado");
        }

        // Validar pontos suficientes
        Integer totalPoints = rewardPointRepository.sumPointsByTutorId(tutorId);
        if (totalPoints == null) totalPoints = 0;
        
        Integer pointsRequired = coupon.getTemplate().getPointsRequired();
        if (totalPoints < pointsRequired) {
            throw new BusinessRuleException("Pontos insuficientes para resgatar este cupom. Necessário: " + pointsRequired);
        }

        // Criar registro de resgate
        Redeem redeem = Redeem.builder()
                .tutor(tutor)
                .coupon(coupon)
                .pointsUsed(pointsRequired)
                .build();
        
        // Atualizar status do cupom
        coupon.setStatus(CouponStatus.RESGATADO);
        couponRepository.save(coupon);

        // Registrar consumo de pontos
        RewardAction rewardAction = rewardActionRepository.findByName("RESGATE_CUPOM")
                .orElseThrow(() -> new EntityNotFoundException("RewardAction", "name", "RESGATE_CUPOM"));

        RewardPoint pointConsumption = RewardPoint.builder()
                .tutor(tutor)
                .rewardAction(rewardAction)
                .points(-pointsRequired)
                .referenceType("REDEEM")
                .referenceId(redeem.getId())
                .build();
        rewardPointRepository.save(pointConsumption);

        return RedeemResponseDTO.builder()
                .id(redeem.getId())
                .pointsUsed(redeem.getPointsUsed())
                .createdAt(redeem.getCreatedAt())
                .tutorId(tutor.getId())
                .tutorName(tutor.getName())
                .couponId(coupon.getId())
                .couponCode(coupon.getCode())
                .build();
    }
}