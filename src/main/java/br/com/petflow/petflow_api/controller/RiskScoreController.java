package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RiskScoreRequestDTO;
import br.com.petflow.petflow_api.dto.RiskScoreResponseDTO;
import br.com.petflow.petflow_api.service.RiskScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "Listar todos os scores de risco")
    public ResponseEntity<List<RiskScoreResponseDTO>> findAll() {
        List<RiskScoreResponseDTO> response = riskScoreService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-pet/{petId}")
    @Operation(summary = "Buscar scores de risco por ID do pet")
    public ResponseEntity<List<RiskScoreResponseDTO>> findByPetId(@PathVariable Long petId) {
        List<RiskScoreResponseDTO> response = riskScoreService.findByPetId(petId);
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