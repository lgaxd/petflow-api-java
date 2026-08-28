package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemCouponRequestDTO {
    @NotNull(message = "O ID do cupom é obrigatório")
    private Long couponId;
}