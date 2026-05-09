package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardPointRequestDTO {

    @NotNull(message = "Os pontos são obrigatórios")
    private Integer points;

    @Size(max = 50, message = "O tipo de referência deve ter no máximo 50 caracteres")
    private String referenceType;

    private Long referenceId;

    @NotNull(message = "O ID do tutor é obrigatório")
    private Long tutorId;

    @NotNull(message = "O ID da ação de recompensa é obrigatório")
    private Long rewardActionId;
}