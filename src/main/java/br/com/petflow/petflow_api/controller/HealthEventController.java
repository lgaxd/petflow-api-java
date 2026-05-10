package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.HealthEventRequestDTO;
import br.com.petflow.petflow_api.dto.HealthEventResponseDTO;
import br.com.petflow.petflow_api.service.HealthEventService;
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
@RequestMapping("/health-events")
@RequiredArgsConstructor
@Tag(name = "Eventos de Saúde", description = "Endpoints para gerenciamento de eventos de saúde dos pets")
public class HealthEventController {

    private final HealthEventService healthEventService;

    @PostMapping
    @Operation(summary = "Criar um novo evento de saúde")
    public ResponseEntity<HealthEventResponseDTO> create(@Valid @RequestBody HealthEventRequestDTO request) {
        HealthEventResponseDTO response = healthEventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento de saúde por ID")
    public ResponseEntity<HealthEventResponseDTO> findById(@PathVariable Long id) {
        HealthEventResponseDTO response = healthEventService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os eventos de saúde com paginação")
    public ResponseEntity<Page<HealthEventResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "eventDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<HealthEventResponseDTO> response = healthEventService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/status")
    @Operation(summary = "Buscar eventos de saúde por status")
    public ResponseEntity<Page<HealthEventResponseDTO>> findByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        Page<HealthEventResponseDTO> response = healthEventService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-pet/{petId}")
    @Operation(summary = "Buscar eventos de saúde por ID do pet")
    public ResponseEntity<Page<HealthEventResponseDTO>> findByPetId(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        Page<HealthEventResponseDTO> response = healthEventService.findByPetId(petId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do evento de saúde")
    public ResponseEntity<HealthEventResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        HealthEventResponseDTO response = healthEventService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar evento de saúde")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        healthEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}