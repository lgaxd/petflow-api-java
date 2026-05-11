package br.com.petflow.petflow_api.dto;

import br.com.petflow.petflow_api.enums.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados da assinatura do pet")
public class SubscriptionResponseDTO {

    @Schema(description = "ID da assinatura", example = "1")
    private Long id;

    @Schema(description = "Data de início", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "Data de término", example = "2025-01-01")
    private LocalDate endDate;

    @Schema(description = "Status da assinatura", example = "ATIVO", allowableValues = {"ATIVO", "ENCERRADO", "CANCELADO", "EXPIRADO"})
    private SubscriptionStatus status;

    @Schema(description = "Data de criação", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID do pet", example = "10")
    private Long petId;

    @Schema(description = "Nome do pet", example = "Rex")
    private String petName;

    @Schema(description = "ID do plano", example = "5")
    private Long planId;

    @Schema(description = "Nome do plano", example = "Plano Premium")
    private String planName;
}