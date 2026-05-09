package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDiscountResponseDTO {

    private Long id;
    private String partnerName;
    private String category;
    private BigDecimal discountPercent;
    private Long clinicId;
    private String clinicName;
}