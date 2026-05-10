package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RiskScoreRequestDTO;
import br.com.petflow.petflow_api.dto.RiskScoreResponseDTO;
import br.com.petflow.petflow_api.service.RiskScoreService;
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
@RequestMapping("/risk-scores")
@RequiredArgsConstructor
@Tag(name = "Scores de Risco", description = "Endpoints para gerenciamento de scores de risco dos pets")
public class RiskScoreController {

    private final RiskScoreService riskScoreService;

    @PostMapping
    @Operation(summary = "Criar um novo score de risco")
    public ResponseEntity<RiskScoreResponseDTO> create(@Valid @RequestBody RiskScoreRequestDTO request) {
        RiskScoreResponseDTO response = riskScoreService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar score de risco por ID")
    public ResponseEntity<RiskScoreResponseDTO> findById(@PathVariable Long id) {
        RiskScoreResponseDTO response = riskScoreService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os scores de risco com paginação")
    public ResponseEntity<Page<RiskScoreResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "calculatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<RiskScoreResponseDTO> response = riskScoreService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-pet/{petId}")
    @Operation(summary = "Buscar scores de risco por ID do pet com paginação")
    public ResponseEntity<Page<RiskScoreResponseDTO>> findByPetId(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "calculatedAt"));
        Page<RiskScoreResponseDTO> response = riskScoreService.findByPetId(petId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest-by-pet/{petId}")
    @Operation(summary = "Buscar último score de risco por ID do pet")
    public ResponseEntity<RiskScoreResponseDTO> getLatestByPetId(@PathVariable Long petId) {
        RiskScoreResponseDTO response = riskScoreService.getLatestByPetId(petId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar score de risco")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        riskScoreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}