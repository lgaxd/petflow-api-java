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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

        if (!COUPON_STATUS_DISPONIVEL.equals(coupon.getStatus())) {
            if (COUPON_STATUS_RESGATADO.equals(coupon.getStatus())) {
                throw new CouponAlreadyRedeemedException(coupon.getCode());
            }
            throw new BusinessRuleException("Cupom não está disponível para resgate");
        }

        if (coupon.getExpirationDate() != null &&
                coupon.getExpirationDate().isBefore(LocalDate.now())) {
            throw new ExpiredCouponException(coupon.getCode(), coupon.getExpirationDate());
        }

        Integer pointsRequired = coupon.getTemplate().getPointsRequired();
        Integer availablePoints = rewardPointService.getTotalPointsByTutor(tutor.getId());

        if (availablePoints < pointsRequired) {
            throw new InsufficientPointsException(availablePoints, pointsRequired);
        }

        Redeem redeem = Redeem.builder()
                .pointsUsed(pointsRequired)
                .tutor(tutor)
                .coupon(coupon)
                .build();

        redeem = redeemRepository.save(redeem);

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

    public Page<RedeemResponseDTO> findAll(Pageable pageable) {
        return redeemRepository.findAllProjected(pageable);
    }

    public Page<RedeemResponseDTO> findAll(Long tutorId, Pageable pageable) {
        if (tutorId != null) {
            return redeemRepository.findByTutorId(tutorId, pageable);
        }
        return redeemRepository.findAllProjected(pageable);
    }

    public Page<RedeemResponseDTO> findByTutorId(Long tutorId, Pageable pageable) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new EntityNotFoundException("Tutor", tutorId);
        }
        return redeemRepository.findByTutorId(tutorId, pageable);
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