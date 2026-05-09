package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequestDTO {

    @NotBlank(message = "O logradouro é obrigatório")
    @Size(max = 200, message = "O logradouro deve ter no máximo 200 caracteres")
    private String street;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String city;

    @NotBlank(message = "O estado é obrigatório")
    @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
    private String state;

    @NotBlank(message = "O CEP é obrigatório")
    @Size(max = 10, message = "O CEP deve ter no máximo 10 caracteres")
    private String zipCode;

    @NotNull(message = "O ID do tutor é obrigatório")
    private Long tutorId;
}