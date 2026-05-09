package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicRequestDTO {

    @NotBlank(message = "O nome da clínica é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String name;

    @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
    private String address;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String phone;

    @NotBlank(message = "O CNPJ é obrigatório")
    @Size(min = 18, max = 18, message = "O CNPJ deve ter 18 caracteres (ex: 00.000.000/0000-00)")
    private String cnpj;
}