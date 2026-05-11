package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.CouponRequestDTO;
import br.com.petflow.petflow_api.dto.CouponResponseDTO;
import br.com.petflow.petflow_api.service.CouponService;
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
@RequestMapping("/coupons")
@Validated
@RequiredArgsConstructor
@Tag(name = "Cupons", description = "Endpoints para gerenciamento de cupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Gerar novo cupom")
    public ResponseEntity<CouponResponseDTO> create(@Valid @RequestBody CouponRequestDTO request) {
        CouponResponseDTO response = couponService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar cupons com filtros")
    public ResponseEntity<Page<CouponResponseDTO>> findAll(
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        return ResponseEntity.ok(couponService.findAll(templateId, status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cupom por ID")
    public ResponseEntity<CouponResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar status do cupom (DISPONIVEL → UTILIZADO)")
    public ResponseEntity<CouponResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(couponService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover cupom")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}