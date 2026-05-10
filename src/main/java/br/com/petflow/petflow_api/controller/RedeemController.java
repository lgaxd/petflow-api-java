package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RedeemRequestDTO;
import br.com.petflow.petflow_api.dto.RedeemResponseDTO;
import br.com.petflow.petflow_api.service.RedeemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redeems")
@RequiredArgsConstructor
@Tag(name = "Resgates", description = "Endpoints para gerenciamento de resgates de cupons")
public class RedeemController {

    private final RedeemService redeemService;

    @PostMapping
    @Operation(summary = "Resgatar um cupom")
    public ResponseEntity<RedeemResponseDTO> create(@Valid @RequestBody RedeemRequestDTO request) {
        RedeemResponseDTO response = redeemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resgate por ID")
    public ResponseEntity<RedeemResponseDTO> findById(@PathVariable Long id) {
        RedeemResponseDTO response = redeemService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os resgates com paginação")
    public ResponseEntity<Page<RedeemResponseDTO>> findAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RedeemResponseDTO> response = redeemService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tutor/{tutorId}")
    @Operation(summary = "Buscar resgates por ID do tutor")
    public ResponseEntity<Page<RedeemResponseDTO>> findByTutorId(
            @PathVariable Long tutorId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<RedeemResponseDTO> response = redeemService.findByTutorId(tutorId, pageable);
        return ResponseEntity.ok(response);
    }
}