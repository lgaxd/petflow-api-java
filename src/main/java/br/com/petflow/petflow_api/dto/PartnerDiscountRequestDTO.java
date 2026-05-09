package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDiscountRequestDTO {

    @NotBlank(message = "O nome do parceiro é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String partnerName;

    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
    private String category;

    @NotNull(message = "O percentual de desconto é obrigatório")
    @DecimalMin(value = "0.01", message = "O desconto deve ser maior que zero")
    @DecimalMax(value = "100.00", message = "O desconto não pode ultrapassar 100%")
    @Digits(integer = 3, fraction = 2, message = "Formato inválido (máximo 5 dígitos, 2 decimais)")
    private BigDecimal discountPercent;

    @NotNull(message = "O ID da clínica é obrigatório")
    private Long clinicId;
}