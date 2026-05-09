package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardActionRequestDTO {

    @NotBlank(message = "O nome da ação é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    private String name;

    @NotNull(message = "O valor de pontos é obrigatório")
    @Positive(message = "O valor de pontos deve ser positivo")
    private Integer pointsValue;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    private String description;
}