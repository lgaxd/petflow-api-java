package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.CouponTemplateRequestDTO;
import br.com.petflow.petflow_api.dto.CouponTemplateResponseDTO;
import br.com.petflow.petflow_api.service.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon-templates")
@RequiredArgsConstructor
@Tag(name = "Templates de Cupom", description = "Endpoints para gerenciamento de templates de cupons")
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;

    @PostMapping
    @Operation(summary = "Criar um novo template de cupom")
    public ResponseEntity<CouponTemplateResponseDTO> create(@Valid @RequestBody CouponTemplateRequestDTO request) {
        CouponTemplateResponseDTO response = couponTemplateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar template de cupom por ID")
    public ResponseEntity<CouponTemplateResponseDTO> findById(@PathVariable Long id) {
        CouponTemplateResponseDTO response = couponTemplateService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os templates de cupom")
    public ResponseEntity<List<CouponTemplateResponseDTO>> findAll() {
        List<CouponTemplateResponseDTO> response = couponTemplateService.findAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar template de cupom")
    public ResponseEntity<CouponTemplateResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CouponTemplateRequestDTO request) {
        CouponTemplateResponseDTO response = couponTemplateService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar template de cupom")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}