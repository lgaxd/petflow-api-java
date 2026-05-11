package br.com.petflow.petflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados da clínica veterinária")
public class ClinicResponseDTO {

    @Schema(description = "ID da clínica", example = "1")
    private Long id;

    @Schema(description = "Nome da clínica", example = "PetCare Veterinária")
    private String name;

    @Schema(description = "Endereço da clínica", example = "Rua das Flores, 123 - São Paulo/SP")
    private String address;

    @Schema(description = "Telefone para contato", example = "(11) 99999-9999")
    private String phone;

    @Schema(description = "CNPJ da clínica", example = "12.345.678/0001-90")
    private String cnpj;

    @Schema(description = "Data de cadastro", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}