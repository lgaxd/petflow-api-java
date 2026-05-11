package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RewardPointRequestDTO;
import br.com.petflow.petflow_api.dto.RewardPointResponseDTO;
import br.com.petflow.petflow_api.service.RewardPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reward-points")
@Validated
@RequiredArgsConstructor
@Tag(name = "Pontos de Recompensa", description = "Endpoints para gerenciamento de pontos")
public class RewardPointController {

    private final RewardPointService rewardPointService;

    @PostMapping
    @Operation(summary = "Adicionar pontos de recompensa")
    public ResponseEntity<RewardPointResponseDTO> create(@Valid @RequestBody RewardPointRequestDTO request) {
        RewardPointResponseDTO response = rewardPointService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar pontuações com filtros")
    public ResponseEntity<Page<RewardPointResponseDTO>> findAll(
            @RequestParam(required = false) Long tutorId,
            @RequestParam(required = false) Long rewardActionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(rewardPointService.findAll(tutorId, rewardActionId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar registro de ponto por ID")
    public ResponseEntity<RewardPointResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(rewardPointService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Corrigir registro de pontos")
    public ResponseEntity<RewardPointResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RewardPointRequestDTO request) {
        return ResponseEntity.ok(rewardPointService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover registro de pontos")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rewardPointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}