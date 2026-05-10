package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RewardPointRequestDTO;
import br.com.petflow.petflow_api.dto.RewardPointResponseDTO;
import br.com.petflow.petflow_api.service.RewardPointService;
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
@RequestMapping("/reward-points")
@RequiredArgsConstructor
@Tag(name = "Pontos de Recompensa", description = "Endpoints para gerenciamento de pontos de recompensa")
public class RewardPointController {

    private final RewardPointService rewardPointService;

    @PostMapping
    @Operation(summary = "Criar um novo registro de pontos")
    public ResponseEntity<RewardPointResponseDTO> create(@Valid @RequestBody RewardPointRequestDTO request) {
        RewardPointResponseDTO response = rewardPointService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pontos por ID")
    public ResponseEntity<RewardPointResponseDTO> findById(@PathVariable Long id) {
        RewardPointResponseDTO response = rewardPointService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tutor/{tutorId}")
    @Operation(summary = "Buscar pontos por ID do tutor com paginação")
    public ResponseEntity<Page<RewardPointResponseDTO>> findByTutorId(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RewardPointResponseDTO> response = rewardPointService.findByTutorId(tutorId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-by-tutor/{tutorId}")
    @Operation(summary = "Buscar total de pontos por ID do tutor")
    public ResponseEntity<Integer> getTotalPointsByTutor(@PathVariable Long tutorId) {
        Integer response = rewardPointService.getTotalPointsByTutor(tutorId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar registro de pontos")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rewardPointService.delete(id);
        return ResponseEntity.noContent().build();
    }
}