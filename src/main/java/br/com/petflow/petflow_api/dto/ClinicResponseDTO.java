package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicResponseDTO {

    private Long id;
    private String name;
    private String address;
    private String phone;
    private String cnpj;
    private LocalDateTime createdAt;
}