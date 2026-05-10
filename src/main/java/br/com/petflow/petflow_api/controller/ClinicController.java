package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.ClinicRequestDTO;
import br.com.petflow.petflow_api.dto.ClinicResponseDTO;
import br.com.petflow.petflow_api.service.ClinicService;
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
@RequestMapping("/clinics")
@RequiredArgsConstructor
@Tag(name = "Clínicas", description = "Endpoints para gerenciamento de clínicas veterinárias")
public class ClinicController {

    private final ClinicService clinicService;

    @PostMapping
    @Operation(summary = "Criar uma nova clínica")
    public ResponseEntity<ClinicResponseDTO> create(@Valid @RequestBody ClinicRequestDTO request) {
        ClinicResponseDTO response = clinicService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar clínica por ID")
    public ResponseEntity<ClinicResponseDTO> findById(@PathVariable Long id) {
        ClinicResponseDTO response = clinicService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as clínicas com paginação")
    public ResponseEntity<Page<ClinicResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ClinicResponseDTO> response = clinicService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar clínicas por nome")
    public ResponseEntity<Page<ClinicResponseDTO>> findByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ClinicResponseDTO> response = clinicService.findByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar clínica")
    public ResponseEntity<ClinicResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ClinicRequestDTO request) {
        ClinicResponseDTO response = clinicService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar clínica")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clinicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}