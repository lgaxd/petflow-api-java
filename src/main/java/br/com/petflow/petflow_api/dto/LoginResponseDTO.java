package br.com.petflow.petflow_api.dto;

import br.com.petflow.petflow_api.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private Long id;
    private String name;
    private String email;
    private UserRole role;
}
