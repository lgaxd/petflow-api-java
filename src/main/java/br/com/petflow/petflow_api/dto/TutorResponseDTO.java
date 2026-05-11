package br.com.petflow.petflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados do tutor")
public class TutorResponseDTO {

    @Schema(description = "ID do tutor", example = "1")
    private Long id;

    @Schema(description = "Nome completo", example = "João Silva")
    private String name;

    @Schema(description = "E-mail do tutor", example = "joao.silva@email.com")
    private String email;

    @Schema(description = "Telefone para contato", example = "(11) 99999-9999")
    private String phone;

    @Schema(description = "Data de cadastro", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}