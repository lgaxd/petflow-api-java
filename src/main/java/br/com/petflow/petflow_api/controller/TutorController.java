package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.TutorRequestDTO;
import br.com.petflow.petflow_api.dto.TutorResponseDTO;
import br.com.petflow.petflow_api.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Endpoints para gerenciamento de tutores")
public class TutorController {

    private final TutorService tutorService;

    @PostMapping
    @Operation(summary = "Criar um novo tutor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tutor criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<TutorResponseDTO> create(@Valid @RequestBody TutorRequestDTO request) {
        TutorResponseDTO response = tutorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tutor encontrado"),
        @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    public ResponseEntity<TutorResponseDTO> findById(@PathVariable Long id) {
        TutorResponseDTO response = tutorService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os tutores com paginação")
    public ResponseEntity<Page<TutorResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<TutorResponseDTO> response = tutorService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar tutores por nome")
    public ResponseEntity<Page<TutorResponseDTO>> findByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<TutorResponseDTO> response = tutorService.findByName(name, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/email")
    @Operation(summary = "Buscar tutor por email")
    public ResponseEntity<TutorResponseDTO> findByEmail(@RequestParam String email) {
        TutorResponseDTO response = tutorService.findByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tutor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tutor atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tutor não encontrado"),
        @ApiResponse(responseCode = "409", description = "Email já cadastrado por outro tutor")
    })
    public ResponseEntity<TutorResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TutorRequestDTO request) {
        TutorResponseDTO response = tutorService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tutor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tutor deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tutorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}