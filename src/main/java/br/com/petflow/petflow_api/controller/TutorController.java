package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.TutorRequestDTO;
import br.com.petflow.petflow_api.dto.TutorResponseDTO;
import br.com.petflow.petflow_api.service.TutorService;
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
@RequestMapping("/tutors")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Endpoints para gerenciamento de tutores")
public class TutorController {

    private final TutorService tutorService;

    @PostMapping
    @Operation(summary = "Cadastrar novo tutor")
    public ResponseEntity<TutorResponseDTO> create(@Valid @RequestBody TutorRequestDTO request) {
        TutorResponseDTO response = tutorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os tutores")
    public ResponseEntity<Page<TutorResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(tutorService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    public ResponseEntity<TutorResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tutorService.findById(id));
    }

    @GetMapping("/{id}/pets")
    @Operation(summary = "Listar todos os pets de um tutor")
    public ResponseEntity<Page<PetResponseDTO>> findPetsByTutorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(tutorService.findPetsByTutorId(id, pageable));
    }

    @GetMapping("/{id}/points")
    @Operation(summary = "Extrato de pontos do tutor")
    public ResponseEntity<Page<RewardPointResponseDTO>> findPointsByTutorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(tutorService.findPointsByTutorId(id, pageable));
    }

    @GetMapping("/{id}/redeems")
    @Operation(summary = "Histórico de cupons resgatados pelo tutor")
    public ResponseEntity<Page<RedeemResponseDTO>> findRedeemsByTutorId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(tutorService.findRedeemsByTutorId(id, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do tutor")
    public ResponseEntity<TutorResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TutorRequestDTO request) {
        return ResponseEntity.ok(tutorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover tutor")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tutorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}