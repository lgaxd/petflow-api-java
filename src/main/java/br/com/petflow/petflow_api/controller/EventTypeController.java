package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.EventTypeRequestDTO;
import br.com.petflow.petflow_api.dto.EventTypeResponseDTO;
import br.com.petflow.petflow_api.service.EventTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/event-types")
@RequiredArgsConstructor
@Tag(name = "Tipos de Evento", description = "Endpoints para gerenciamento de tipos de eventos de saúde")
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @PostMapping
    @Operation(summary = "Criar um novo tipo de evento")
    public ResponseEntity<EventTypeResponseDTO> create(@Valid @RequestBody EventTypeRequestDTO request) {
        EventTypeResponseDTO response = eventTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de evento por ID")
    public ResponseEntity<EventTypeResponseDTO> findById(@PathVariable Long id) {
        EventTypeResponseDTO response = eventTypeService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os tipos de evento")
    public ResponseEntity<List<EventTypeResponseDTO>> findAll() {
        List<EventTypeResponseDTO> response = eventTypeService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/category")
    @Operation(summary = "Buscar tipos de evento por categoria")
    public ResponseEntity<List<EventTypeResponseDTO>> findByCategory(@RequestParam String category) {
        List<EventTypeResponseDTO> response = eventTypeService.findByCategory(category);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/name")
    @Operation(summary = "Buscar tipos de evento por nome")
    public ResponseEntity<List<EventTypeResponseDTO>> findByName(@RequestParam String name) {
        List<EventTypeResponseDTO> response = eventTypeService.findByName(name);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de evento")
    public ResponseEntity<EventTypeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EventTypeRequestDTO request) {
        EventTypeResponseDTO response = eventTypeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar tipo de evento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}