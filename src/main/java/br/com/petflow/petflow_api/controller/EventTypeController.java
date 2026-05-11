package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.EventTypeRequestDTO;
import br.com.petflow.petflow_api.dto.EventTypeResponseDTO;
import br.com.petflow.petflow_api.service.EventTypeService;
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
@RequestMapping("/event-types")
@RequiredArgsConstructor
@Tag(name = "Tipos de Evento", description = "Endpoints para gerenciamento de tipos de evento")
public class EventTypeController {

    private final EventTypeService eventTypeService;

    @PostMapping
    @Operation(summary = "Cadastrar novo tipo de evento")
    public ResponseEntity<EventTypeResponseDTO> create(@Valid @RequestBody EventTypeRequestDTO request) {
        EventTypeResponseDTO response = eventTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os tipos de evento")
    public ResponseEntity<Page<EventTypeResponseDTO>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(eventTypeService.findAll(category, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de evento por ID")
    public ResponseEntity<EventTypeResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(eventTypeService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de evento")
    public ResponseEntity<EventTypeResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EventTypeRequestDTO request) {
        return ResponseEntity.ok(eventTypeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover tipo de evento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}