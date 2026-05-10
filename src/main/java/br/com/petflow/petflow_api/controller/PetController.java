package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.PetRequestDTO;
import br.com.petflow.petflow_api.dto.PetResponseDTO;
import br.com.petflow.petflow_api.service.PetService;
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
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Endpoints para gerenciamento de pets")
public class PetController {

    private final PetService petService;

    @PostMapping
    @Operation(summary = "Criar um novo pet")
    public ResponseEntity<PetResponseDTO> create(@Valid @RequestBody PetRequestDTO request) {
        PetResponseDTO response = petService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public ResponseEntity<PetResponseDTO> findById(@PathVariable Long id) {
        PetResponseDTO response = petService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os pets com paginação")
    public ResponseEntity<Page<PetResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PetResponseDTO> response = petService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar pets por nome")
    public ResponseEntity<Page<PetResponseDTO>> findByName(
            @RequestParam String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PetResponseDTO> response = petService.findByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tutor/{tutorId}")
    @Operation(summary = "Buscar pets por ID do tutor")
    public ResponseEntity<Page<PetResponseDTO>> findByTutorId(
            @PathVariable Long tutorId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PetResponseDTO> response = petService.findByTutorId(tutorId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-species/{speciesId}")
    @Operation(summary = "Buscar pets por ID da espécie")
    public ResponseEntity<Page<PetResponseDTO>> findBySpeciesId(
            @PathVariable Long speciesId,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PetResponseDTO> response = petService.findBySpeciesId(speciesId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet")
    public ResponseEntity<PetResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO request) {
        PetResponseDTO response = petService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pet")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }
}