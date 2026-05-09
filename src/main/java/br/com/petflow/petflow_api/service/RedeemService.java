package br.com.petflow.petflow_api.service;

import br.com.petflow.petflow_api.dto.RedeemRequestDTO;
import br.com.petflow.petflow_api.dto.RedeemResponseDTO;
import br.com.petflow.petflow_api.entity.Coupon;
import br.com.petflow.petflow_api.entity.Redeem;
import br.com.petflow.petflow_api.entity.Tutor;
import br.com.petflow.petflow_api.exception.*;
import br.com.petflow.petflow_api.repository.CouponRepository;
import br.com.petflow.petflow_api.repository.RedeemRepository;
import br.com.petflow.petflow_api.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedeemService {

    private final RedeemRepository redeemRepository;
    private final TutorRepository tutorRepository;
    private final CouponRepository couponRepository;
    private final RewardPointService rewardPointService;

    private static final String COUPON_STATUS_DISPONIVEL = "DISPONIVEL";
    private static final String COUPON_STATUS_RESGATADO = "RESGATADO";

    @Transactional
    @CacheEvict(value = "redeems", allEntries = true)
    public RedeemResponseDTO create(RedeemRequestDTO request) {
        Tutor tutor = tutorRepository.findById(request.getTutorId())
                .orElseThrow(() -> new EntityNotFoundException("Tutor", request.getTutorId()));

        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new EntityNotFoundException("Cupom", request.getCouponId()));

        // Validar status do cupom
        if (!COUPON_STATUS_DISPONIVEL.equals(coupon.getStatus())) {
            if (COUPON_STATUS_RESGATADO.equals(coupon.getStatus())) {
                throw new CouponAlreadyRedeemedException(coupon.getCode());
            }
            throw new BusinessRuleException("Cupom não está disponível para resgate");
        }

        // Validar expiração
        if (coupon.getExpirationDate() != null && 
            coupon.getExpirationDate().isBefore(LocalDate.now())) {
            throw new ExpiredCouponException(coupon.getCode(), coupon.getExpirationDate());
        }

        // Validar pontos suficientes
        Integer pointsRequired = coupon.getTemplate().getPointsRequired();
        Integer availablePoints = rewardPointService.getTotalPointsByTutor(tutor.getId());

        if (availablePoints < pointsRequired) {
            throw new InsufficientPointsException(availablePoints, pointsRequired);
        }

        // Criar resgate
        Redeem redeem = Redeem.builder()
                .pointsUsed(pointsRequired)
                .tutor(tutor)
                .coupon(coupon)
                .build();

        redeem = redeemRepository.save(redeem);

        // Atualizar status do cupom
        coupon.setStatus(COUPON_STATUS_RESGATADO);
        couponRepository.save(coupon);

        return toResponseDTO(redeem);
    }

    @Cacheable(value = "redeems", key = "#id")
    public RedeemResponseDTO findById(Long id) {
        Redeem redeem = redeemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Resgate", id));
        return toResponseDTO(redeem);
    }

    public List<RedeemResponseDTO> findAll() {
        return redeemRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<RedeemResponseDTO> findByTutorId(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return redeemRepository.findByTutorId(tutorId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private RedeemResponseDTO toResponseDTO(Redeem redeem) {
        return RedeemResponseDTO.builder()
                .id(redeem.getId())
                .pointsUsed(redeem.getPointsUsed())
                .createdAt(redeem.getCreatedAt())
                .tutorId(redeem.getTutor().getId())
                .tutorName(redeem.getTutor().getName())
                .couponId(redeem.getCoupon().getId())
                .couponCode(redeem.getCoupon().getCode())
                .build();
    }
}