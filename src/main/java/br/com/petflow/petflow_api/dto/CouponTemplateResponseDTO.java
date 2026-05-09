package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponTemplateResponseDTO {

    private Long id;
    private String title;
    private BigDecimal discountValue;
    private String discountType;
    private Integer pointsRequired;
    private Long partnerDiscountId;
    private String partnerName;
}