package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.ClinicRequestDTO;
import br.com.petflow.petflow_api.dto.ClinicResponseDTO;
import br.com.petflow.petflow_api.dto.PlanResponseDTO;
import br.com.petflow.petflow_api.service.ClinicService;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clínicas", description = "Endpoints para gerenciamento de clínicas")
public class ClinicController {

    private final ClinicService clinicService;

    @PostMapping
    @Operation(summary = "Cadastrar nova clínica")
    public ResponseEntity<ClinicResponseDTO> create(@Valid @RequestBody ClinicRequestDTO request) {
        ClinicResponseDTO response = clinicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as clínicas")
    public ResponseEntity<Page<ClinicResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(clinicService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar clínica por ID")
    public ResponseEntity<ClinicResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clinicService.findById(id));
    }

    @GetMapping("/{id}/plans")
    @Operation(summary = "Listar planos de uma clínica")
    public ResponseEntity<Page<PlanResponseDTO>> findPlansByClinicId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(clinicService.findPlansByClinicId(id, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar clínica")
    public ResponseEntity<ClinicResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRequestDTO request) {
        return ResponseEntity.ok(clinicService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover clínica")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clinicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}