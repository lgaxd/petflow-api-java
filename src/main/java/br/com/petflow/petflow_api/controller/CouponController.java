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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Tag(name = "Cupons", description = "Endpoints para gerenciamento de cupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Criar um novo cupom")
    public ResponseEntity<CouponResponseDTO> create(@Valid @RequestBody CouponRequestDTO request) {
        CouponResponseDTO response = couponService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cupom por ID")
    public ResponseEntity<CouponResponseDTO> findById(@PathVariable Long id) {
        CouponResponseDTO response = couponService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/code")
    @Operation(summary = "Buscar cupom por código")
    public ResponseEntity<CouponResponseDTO> findByCode(@RequestParam String code) {
        CouponResponseDTO response = couponService.findByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os cupons com paginação")
    public ResponseEntity<Page<CouponResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<CouponResponseDTO> response = couponService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/status")
    @Operation(summary = "Buscar cupons por status")
    public ResponseEntity<Page<CouponResponseDTO>> findByStatus(
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<CouponResponseDTO> response = couponService.findByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cupom")
    public ResponseEntity<CouponResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequestDTO request) {
        CouponResponseDTO response = couponService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar cupom")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.delete(id);
        return ResponseEntity.noContent().build();
    }
}