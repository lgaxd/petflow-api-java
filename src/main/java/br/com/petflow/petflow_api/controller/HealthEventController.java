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
@Tag(name = "Eventos de Saúde", description = "Endpoints para gerenciamento de eventos de saúde")
public class HealthEventController {

    private final HealthEventService healthEventService;

    @PostMapping
    @Operation(summary = "Registrar novo evento de saúde")
    public ResponseEntity<HealthEventResponseDTO> create(@Valid @RequestBody HealthEventRequestDTO request) {
        HealthEventResponseDTO response = healthEventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar eventos de saúde com filtros")
    public ResponseEntity<Page<HealthEventResponseDTO>> findAll(
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long eventTypeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "eventDate"));
        return ResponseEntity.ok(healthEventService.findAll(petId, clinicId, status, eventTypeId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento de saúde por ID")
    public ResponseEntity<HealthEventResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(healthEventService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento de saúde (ex: marcar como REALIZADO)")
    public ResponseEntity<HealthEventResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody HealthEventRequestDTO request) {
        return ResponseEntity.ok(healthEventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover evento de saúde")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        healthEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}