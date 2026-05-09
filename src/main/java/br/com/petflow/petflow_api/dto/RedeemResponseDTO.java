package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedeemResponseDTO {

    private Long id;
    private Integer pointsUsed;
    private LocalDateTime createdAt;
    private Long tutorId;
    private String tutorName;
    private Long couponId;
    private String couponCode;
}