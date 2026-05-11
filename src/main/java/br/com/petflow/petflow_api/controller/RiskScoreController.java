package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RiskScoreRequestDTO;
import br.com.petflow.petflow_api.dto.RiskScoreResponseDTO;
import br.com.petflow.petflow_api.service.RiskScoreService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/risk-scores")
@RequiredArgsConstructor
@Tag(name = "Scores de Risco", description = "Endpoints para gerenciamento de scores de risco")
public class RiskScoreController {

    private final RiskScoreService riskScoreService;

    @PostMapping
    @Operation(summary = "Registrar score de risco calculado")
    public ResponseEntity<RiskScoreResponseDTO> create(@Valid @RequestBody RiskScoreRequestDTO request) {
        RiskScoreResponseDTO response = riskScoreService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar scores de risco com filtro por pet")
    public ResponseEntity<Page<RiskScoreResponseDTO>> findAll(
            @RequestParam(required = false) Long petId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "calculatedAt"));
        return ResponseEntity.ok(riskScoreService.findAll(petId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar score de risco por ID")
    public ResponseEntity<RiskScoreResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(riskScoreService.findById(id));
    }
}