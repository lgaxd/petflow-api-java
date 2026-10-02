package br.com.petflow.petflow_api.dto;

import br.com.petflow.petflow_api.enums.UserRole;
import lombok.Builder;

@Builder
public record LoginResponseDTO(
    String token,
    Long id,
    String name,
    String email,
    UserRole role
) {}
