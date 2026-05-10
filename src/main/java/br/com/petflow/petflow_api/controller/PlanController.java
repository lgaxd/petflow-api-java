package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.PlanRequestDTO;
import br.com.petflow.petflow_api.dto.PlanResponseDTO;
import br.com.petflow.petflow_api.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@Tag(name = "Planos", description = "Endpoints para gerenciamento de planos")
public class PlanController {

    private final PlanService planService;

    @PostMapping
    @Operation(summary = "Criar um novo plano")
    public ResponseEntity<PlanResponseDTO> create(@Valid @RequestBody PlanRequestDTO request) {
        PlanResponseDTO response = planService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar plano por ID")
    public ResponseEntity<PlanResponseDTO> findById(@PathVariable Long id) {
        PlanResponseDTO response = planService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os planos com paginação")
    public ResponseEntity<Page<PlanResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PlanResponseDTO> response = planService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar planos por nome")
    public ResponseEntity<Page<PlanResponseDTO>> findByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PlanResponseDTO> response = planService.findByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-clinic/{clinicId}")
    @Operation(summary = "Buscar planos por ID da clínica")
    public ResponseEntity<Page<PlanResponseDTO>> findByClinicId(
            @PathVariable Long clinicId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PlanResponseDTO> response = planService.findByClinicId(clinicId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar plano")
    public ResponseEntity<PlanResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlanRequestDTO request) {
        PlanResponseDTO response = planService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar plano")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        planService.delete(id);
        return ResponseEntity.noContent().build();
    }
}