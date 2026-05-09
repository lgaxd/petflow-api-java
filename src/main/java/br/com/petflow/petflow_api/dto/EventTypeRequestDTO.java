package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTypeRequestDTO {

    @NotBlank(message = "O nome do tipo de evento é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    private String name;

    @NotNull(message = "O valor de pontos é obrigatório")
    @PositiveOrZero(message = "Os pontos de recompensa não podem ser negativos")
    private Integer pointsReward;

    @NotBlank(message = "A categoria é obrigatória")
    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
    private String category;
}