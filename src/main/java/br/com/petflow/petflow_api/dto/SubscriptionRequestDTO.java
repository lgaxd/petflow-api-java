package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionRequestDTO {

    @NotNull(message = "A data de início é obrigatória")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    private String status;

    @NotNull(message = "O ID do pet é obrigatório")
    private Long petId;

    @NotNull(message = "O ID do plano é obrigatório")
    private Long planId;
}