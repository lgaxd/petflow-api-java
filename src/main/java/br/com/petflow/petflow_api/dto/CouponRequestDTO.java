package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequestDTO {

    @NotBlank(message = "O código do cupom é obrigatório")
    @Size(max = 50, message = "O código deve ter no máximo 50 caracteres")
    private String code;

    @NotBlank(message = "O status é obrigatório")
    @Size(max = 20, message = "O status deve ter no máximo 20 caracteres")
    private String status;

    @Future(message = "A data de expiração deve ser uma data futura")
    private LocalDate expirationDate;

    @NotNull(message = "O ID do template é obrigatório")
    private Long templateId;
}