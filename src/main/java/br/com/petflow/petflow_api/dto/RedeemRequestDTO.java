package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemRequestDTO {

    @NotNull(message = "Os pontos utilizados são obrigatórios")
    @Positive(message = "Os pontos utilizados devem ser positivos")
    private Integer pointsUsed;

    @NotNull(message = "O ID do tutor é obrigatório")
    private Long tutorId;

    @NotNull(message = "O ID do cupom é obrigatório")
    private Long couponId;
}