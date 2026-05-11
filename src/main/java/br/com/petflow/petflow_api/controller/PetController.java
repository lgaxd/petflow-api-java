package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.PetRequestDTO;
import br.com.petflow.petflow_api.dto.PetResponseDTO;
import br.com.petflow.petflow_api.service.PetService;
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
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Endpoints para gerenciamento de pets")
public class PetController {

    private final PetService petService;

    @PostMapping
    @Operation(summary = "Cadastrar novo pet")
    public ResponseEntity<PetResponseDTO> create(@Valid @RequestBody PetRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petService.create(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos os pets com filtros")
    public ResponseEntity<Page<PetResponseDTO>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ResponseEntity.ok(petService.findAll(name, tutorId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public ResponseEntity<PetResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do pet")
    public ResponseEntity<PetResponseDTO> update(@PathVariable Long id, @Valid @RequestBody PetRequestDTO request) {
        return ResponseEntity.ok(petService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover pet")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }
}