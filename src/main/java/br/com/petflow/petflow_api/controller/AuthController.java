package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.LoginRequestDTO;
import br.com.petflow.petflow_api.dto.LoginResponseDTO;
import br.com.petflow.petflow_api.security.AuthenticatedTutor;
import br.com.petflow.petflow_api.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica um tutor ou admin e retorna um token JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        AuthenticatedTutor tutor = (AuthenticatedTutor) authentication.getPrincipal();
        String token = jwtService.generateToken(tutor);

        return ResponseEntity.ok(LoginResponseDTO.builder()
                .token(token)
                .id(tutor.getId())
                .name(tutor.getName())
                .email(tutor.getEmail())
                .role(tutor.getRole())
                .build());
    }
}
