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
        PetResponseDTO response = petService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os pets com filtros opcionais")
    public ResponseEntity<Page<PetResponseDTO>> findAll(
            @RequestParam(required = false) Long tutorId,
            @RequestParam(required = false) Long speciesId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(petService.findAll(tutorId, speciesId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public ResponseEntity<PetResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(petService.findById(id));
    }

    @GetMapping("/{id}/health-events")
    @Operation(summary = "Histórico de eventos de saúde do pet")
    public ResponseEntity<Page<HealthEventResponseDTO>> findHealthEventsByPetId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        return ResponseEntity.ok(petService.findHealthEventsByPetId(id, pageable));
    }

    @GetMapping("/{id}/risk-scores")
    @Operation(summary = "Histórico de scores de risco do pet")
    public ResponseEntity<Page<RiskScoreResponseDTO>> findRiskScoresByPetId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "calculatedAt"));
        return ResponseEntity.ok(petService.findRiskScoresByPetId(id, pageable));
    }

    @GetMapping("/{id}/subscriptions")
    @Operation(summary = "Assinaturas ativas e anteriores do pet")
    public ResponseEntity<Page<SubscriptionResponseDTO>> findSubscriptionsByPetId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(petService.findSubscriptionsByPetId(id, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do pet")
    public ResponseEntity<PetResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO request) {
        return ResponseEntity.ok(petService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover pet")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ResponseEntity.noContent().build();
    }
}