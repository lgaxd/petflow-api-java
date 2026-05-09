package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponTemplateRequestDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
    private String title;

    @NotNull(message = "O valor do desconto é obrigatório")
    @DecimalMin(value = "0.01", message = "O desconto deve ser maior que zero")
    @Digits(integer = 8, fraction = 2, message = "Formato inválido (máximo 10 dígitos, 2 decimais)")
    private BigDecimal discountValue;

    @NotBlank(message = "O tipo de desconto é obrigatório")
    @Size(max = 20, message = "O tipo deve ter no máximo 20 caracteres")
    private String discountType;

    @NotNull(message = "Os pontos necessários são obrigatórios")
    @Positive(message = "Os pontos necessários devem ser positivos")
    private Integer pointsRequired;

    @NotNull(message = "O ID do desconto de parceiro é obrigatório")
    private Long partnerDiscountId;
}