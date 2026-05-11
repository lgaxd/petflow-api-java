package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthEventRequestDTO {

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    private String description;

    @NotNull(message = "A data do evento é obrigatória")
    private LocalDate eventDate;

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    private String status;

    @NotNull(message = "O ID do pet é obrigatório")
    private Long petId;

    @NotBlank(message = "O tipo de evento é obrigatório")
    private String eventType;

    private Long clinicId;
}