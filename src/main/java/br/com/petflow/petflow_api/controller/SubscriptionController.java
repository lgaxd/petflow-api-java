package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.SubscriptionRequestDTO;
import br.com.petflow.petflow_api.dto.SubscriptionResponseDTO;
import br.com.petflow.petflow_api.service.SubscriptionService;
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
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Assinaturas", description = "Endpoints para gerenciamento de assinaturas de planos")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Criar uma nova assinatura")
    public ResponseEntity<SubscriptionResponseDTO> create(@Valid @RequestBody SubscriptionRequestDTO request) {
        SubscriptionResponseDTO response = subscriptionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar assinatura por ID")
    public ResponseEntity<SubscriptionResponseDTO> findById(@PathVariable Long id) {
        SubscriptionResponseDTO response = subscriptionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as assinaturas com paginação")
    public ResponseEntity<Page<SubscriptionResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<SubscriptionResponseDTO> response = subscriptionService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/status")
    @Operation(summary = "Buscar assinaturas por status")
    public ResponseEntity<Page<SubscriptionResponseDTO>> findByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SubscriptionResponseDTO> response = subscriptionService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-pet/{petId}")
    @Operation(summary = "Buscar assinaturas por ID do pet")
    public ResponseEntity<Page<SubscriptionResponseDTO>> findByPetId(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SubscriptionResponseDTO> response = subscriptionService.findByPetId(petId, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar assinatura")
    public ResponseEntity<SubscriptionResponseDTO> cancel(@PathVariable Long id) {
        SubscriptionResponseDTO response = subscriptionService.cancel(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar assinatura")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}