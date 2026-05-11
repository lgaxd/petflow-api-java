package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RedeemRequestDTO;
import br.com.petflow.petflow_api.dto.RedeemResponseDTO;
import br.com.petflow.petflow_api.service.RedeemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redeems")
@RequiredArgsConstructor
@Tag(name = "Resgates", description = "Endpoints para gerenciamento de resgates de cupons")
public class RedeemController {

    private final RedeemService redeemService;

    @PostMapping
    @Operation(summary = "Registrar resgate", description = "Realiza o resgate de um cupom utilizando pontos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resgate realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Tutor ou cupom não encontrado"),
        @ApiResponse(responseCode = "422", description = "Cupom expirado, já resgatado ou pontos insuficientes")
    })
    public ResponseEntity<RedeemResponseDTO> create(@Valid @RequestBody RedeemRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(redeemService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar resgates", description = "Retorna lista paginada de resgates com filtros")
    public ResponseEntity<Page<RedeemResponseDTO>> findAll(
            @RequestParam(required = false) Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(redeemService.findAll(tutorId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resgate por ID", description = "Retorna os dados de um resgate específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resgate encontrado"),
        @ApiResponse(responseCode = "404", description = "Resgate não encontrado")
    })
    public ResponseEntity<RedeemResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(redeemService.findById(id));
    }
}