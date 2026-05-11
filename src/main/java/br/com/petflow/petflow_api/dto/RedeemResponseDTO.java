package br.com.petflow.petflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados do resgate de cupom")
public class RedeemResponseDTO {

    @Schema(description = "ID do resgate", example = "1")
    private Long id;

    @Schema(description = "Pontos utilizados no resgate", example = "100")
    private Integer pointsUsed;

    @Schema(description = "Data do resgate", example = "2024-06-15T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID do tutor", example = "10")
    private Long tutorId;

    @Schema(description = "Nome do tutor", example = "João Silva")
    private String tutorName;

    @Schema(description = "ID do cupom resgatado", example = "20")
    private Long couponId;

    @Schema(description = "Código do cupom", example = "DESCONTO20")
    private String couponCode;
}