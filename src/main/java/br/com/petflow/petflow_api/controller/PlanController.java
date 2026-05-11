package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.PlanRequestDTO;
import br.com.petflow.petflow_api.dto.PlanResponseDTO;
import br.com.petflow.petflow_api.service.PlanService;
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
@RequestMapping("/plans")
@Validated
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Endpoints para gerenciamento de planos")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @Operation(summary = "Criar novo plano")
    public ResponseEntity<PlanResponseDTO> create(@Valid @RequestBody PlanRequestDTO request) {
        PlanResponseDTO response = planService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar planos com filtro opcional por clínica")
    public ResponseEntity<Page<PlanResponseDTO>> findAll(
            @RequestParam(required = false) Long clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(planService.findAll(clinicId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar plano por ID")
    public ResponseEntity<PlanResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(planService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar plano")
    public ResponseEntity<PlanResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequestDTO request) {
        return ResponseEntity.ok(planService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover plano")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return ResponseEntity.noContent().build();
    }
}