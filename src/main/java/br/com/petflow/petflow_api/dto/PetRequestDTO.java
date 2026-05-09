package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetRequestDTO {

    @NotBlank(message = "O nome do pet é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String name;

    @Size(max = 50, message = "A raça deve ter no máximo 50 caracteres")
    private String breed;

    @Past(message = "A data de nascimento deve ser uma data passada")
    private LocalDate birthDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "O peso deve ser positivo")
    @Digits(integer = 3, fraction = 2, message = "Formato de peso inválido (máximo 5 dígitos, 2 decimais)")
    private BigDecimal weight;

    @NotNull(message = "O ID do tutor é obrigatório")
    private Long tutorId;

    @NotNull(message = "O ID da espécie é obrigatório")
    private Long speciesId;
}