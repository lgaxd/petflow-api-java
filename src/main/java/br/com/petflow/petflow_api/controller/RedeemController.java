package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.RedeemRequestDTO;
import br.com.petflow.petflow_api.dto.RedeemResponseDTO;
import br.com.petflow.petflow_api.service.RedeemService;
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
@RequestMapping("/redeems")
@Validated
@RequiredArgsConstructor
@Tag(name = "Resgates", description = "Endpoints para gerenciamento de resgates de cupons")
public class RedeemController {

    private final RedeemService redeemService;

    @PostMapping
    @Operation(summary = "Registrar resgate de cupom")
    public ResponseEntity<RedeemResponseDTO> create(@Valid @RequestBody RedeemRequestDTO request) {
        RedeemResponseDTO response = redeemService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar resgates com filtros")
    public ResponseEntity<Page<RedeemResponseDTO>> findAll(
            @RequestParam(required = false) Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(redeemService.findAll(tutorId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar resgate por ID")
    public ResponseEntity<RedeemResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(redeemService.findById(id));
    }
}