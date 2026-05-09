package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskLevelRequestDTO {

    @NotBlank(message = "O nome do nível de risco é obrigatório")
    @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
    private String name;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    private String description;

    @NotNull(message = "O score mínimo é obrigatório")
    @PositiveOrZero(message = "O score mínimo não pode ser negativo")
    private Integer minScore;

    @NotNull(message = "O score máximo é obrigatório")
    @Positive(message = "O score máximo deve ser positivo")
    private Integer maxScore;
}