package br.com.petflow.petflow_api.dto;

import br.com.petflow.petflow_api.enums.CouponStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados do cupom de desconto")
public class CouponResponseDTO {

    @Schema(description = "ID do cupom", example = "1")
    private Long id;

    @Schema(description = "Código único do cupom", example = "DESCONTO20")
    private String code;

    @Schema(description = "Status do cupom", example = "DISPONIVEL", allowableValues = {"DISPONIVEL", "RESGATADO", "UTILIZADO"})
    private CouponStatus status;

    @Schema(description = "Data de expiração", example = "2025-12-31")
    private LocalDate expirationDate;

    @Schema(description = "Valor do desconto", example = "20.00")
    private BigDecimal discountValue;

    @Schema(description = "Pontos necessários para resgate", example = "100")
    private Integer pointsRequired;

    @Schema(description = "Data de criação", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}