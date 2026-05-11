package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RiskLevelRequestDTO;
import br.com.petflow.petflow_api.dto.RiskLevelResponseDTO;
import br.com.petflow.petflow_api.service.RiskLevelService;
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
@RequestMapping("/risk-levels")
@Validated
@RequiredArgsConstructor
@Tag(name = "Níveis de Risco", description = "Endpoints para gerenciamento de níveis de risco")
public class RiskLevelController {

    private final RiskLevelService riskLevelService;

    @PostMapping
    @Operation(summary = "Criar um novo nível de risco")
    public ResponseEntity<RiskLevelResponseDTO> create(@Valid @RequestBody RiskLevelRequestDTO request) {
        RiskLevelResponseDTO response = riskLevelService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar nível de risco por ID")
    public ResponseEntity<RiskLevelResponseDTO> findById(@PathVariable Long id) {
        RiskLevelResponseDTO response = riskLevelService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os níveis de risco com paginação")
    public ResponseEntity<Page<RiskLevelResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "minScore") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<RiskLevelResponseDTO> response = riskLevelService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar nível de risco por nome")
    public ResponseEntity<RiskLevelResponseDTO> findByName(@RequestParam String name) {
        RiskLevelResponseDTO response = riskLevelService.findByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar nível de risco")
    public ResponseEntity<RiskLevelResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RiskLevelRequestDTO request) {
        RiskLevelResponseDTO response = riskLevelService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar nível de risco")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        riskLevelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}