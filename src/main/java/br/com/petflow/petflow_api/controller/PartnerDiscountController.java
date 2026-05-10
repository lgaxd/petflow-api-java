package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.PartnerDiscountRequestDTO;
import br.com.petflow.petflow_api.dto.PartnerDiscountResponseDTO;
import br.com.petflow.petflow_api.service.PartnerDiscountService;
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
    @Operation(summary = "Listar todos os descontos de parceiros com paginação")
    public ResponseEntity<Page<PartnerDiscountResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "partnerName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<PartnerDiscountResponseDTO> response = partnerDiscountService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/category")
    @Operation(summary = "Buscar descontos de parceiros por categoria")
    public ResponseEntity<Page<PartnerDiscountResponseDTO>> findByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "partnerName"));
        Page<PartnerDiscountResponseDTO> response = partnerDiscountService.findByCategory(category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-clinic/{clinicId}")
    @Operation(summary = "Buscar descontos de parceiros por ID da clínica")
    public ResponseEntity<Page<PartnerDiscountResponseDTO>> findByClinicId(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "partnerName"));
        Page<PartnerDiscountResponseDTO> response = partnerDiscountService.findByClinicId(clinicId, pageable);
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