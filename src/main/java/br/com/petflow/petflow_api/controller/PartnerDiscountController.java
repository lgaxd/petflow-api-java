package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.PartnerDiscountRequestDTO;
import br.com.petflow.petflow_api.dto.PartnerDiscountResponseDTO;
import br.com.petflow.petflow_api.service.PartnerDiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partner-discounts")
@RequiredArgsConstructor
@Tag(name = "Descontos de Parceiros", description = "Endpoints para gerenciamento de descontos de parceiros")
public class PartnerDiscountController {

    private final PartnerDiscountService partnerDiscountService;

    @PostMapping
    @Operation(summary = "Criar um novo desconto de parceiro")
    public ResponseEntity<PartnerDiscountResponseDTO> create(@Valid @RequestBody PartnerDiscountRequestDTO request) {
        PartnerDiscountResponseDTO response = partnerDiscountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar desconto de parceiro por ID")
    public ResponseEntity<PartnerDiscountResponseDTO> findById(@PathVariable Long id) {
        PartnerDiscountResponseDTO response = partnerDiscountService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os descontos de parceiros")
    public ResponseEntity<List<PartnerDiscountResponseDTO>> findAll() {
        List<PartnerDiscountResponseDTO> response = partnerDiscountService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/category")
    @Operation(summary = "Buscar descontos de parceiros por categoria")
    public ResponseEntity<List<PartnerDiscountResponseDTO>> findByCategory(@RequestParam String category) {
        List<PartnerDiscountResponseDTO> response = partnerDiscountService.findByCategory(category);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar desconto de parceiro")
    public ResponseEntity<PartnerDiscountResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PartnerDiscountRequestDTO request) {
        PartnerDiscountResponseDTO response = partnerDiscountService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar desconto de parceiro")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partnerDiscountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}