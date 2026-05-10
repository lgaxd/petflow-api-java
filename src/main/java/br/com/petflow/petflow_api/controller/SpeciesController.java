package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.SpeciesRequestDTO;
import br.com.petflow.petflow_api.dto.SpeciesResponseDTO;
import br.com.petflow.petflow_api.service.SpeciesService;
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
@RequestMapping("/species")
@RequiredArgsConstructor
@Tag(name = "Espécies", description = "Endpoints para gerenciamento de espécies de pets")
public class SpeciesController {

    private final SpeciesService speciesService;

    @PostMapping
    @Operation(summary = "Criar uma nova espécie")
    public ResponseEntity<SpeciesResponseDTO> create(@Valid @RequestBody SpeciesRequestDTO request) {
        SpeciesResponseDTO response = speciesService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar espécie por ID")
    public ResponseEntity<SpeciesResponseDTO> findById(@PathVariable Long id) {
        SpeciesResponseDTO response = speciesService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as espécies com paginação")
    public ResponseEntity<Page<SpeciesResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<SpeciesResponseDTO> response = speciesService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar espécie")
    public ResponseEntity<SpeciesResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody SpeciesRequestDTO request) {
        SpeciesResponseDTO response = speciesService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar espécie")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        speciesService.delete(id);
        return ResponseEntity.noContent().build();
    }
}