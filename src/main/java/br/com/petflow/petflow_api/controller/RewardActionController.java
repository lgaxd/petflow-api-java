package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RewardActionRequestDTO;
import br.com.petflow.petflow_api.dto.RewardActionResponseDTO;
import br.com.petflow.petflow_api.service.RewardActionService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reward-actions")
@Validated
@RequiredArgsConstructor
@Tag(name = "Ações de Recompensa", description = "Endpoints para gerenciamento de ações que geram pontos")
public class RewardActionController {

    private final RewardActionService rewardActionService;

    @PostMapping
    @Operation(summary = "Criar uma nova ação de recompensa")
    public ResponseEntity<RewardActionResponseDTO> create(@Valid @RequestBody RewardActionRequestDTO request) {
        RewardActionResponseDTO response = rewardActionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ação de recompensa por ID")
    public ResponseEntity<RewardActionResponseDTO> findById(@PathVariable Long id) {
        RewardActionResponseDTO response = rewardActionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todas as ações de recompensa com paginação")
    public ResponseEntity<Page<RewardActionResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<RewardActionResponseDTO> response = rewardActionService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar ação de recompensa")
    public ResponseEntity<RewardActionResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RewardActionRequestDTO request) {
        RewardActionResponseDTO response = rewardActionService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar ação de recompensa")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rewardActionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}