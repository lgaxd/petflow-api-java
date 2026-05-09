package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponseDTO {

    private Long id;
    private String code;
    private String status;
    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private Long templateId;
    private String templateTitle;
}