package br.com.petflow.petflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponCatalogDTO {
    private Long id;
    private String code;
    private String title;
    private String description;
    private Integer pointsRequired;
    private String discountType;
    private Double discountValue;
    private LocalDate expirationDate;
    private Boolean available;
}