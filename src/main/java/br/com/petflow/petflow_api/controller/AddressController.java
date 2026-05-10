package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.AddressRequestDTO;
import br.com.petflow.petflow_api.dto.AddressResponseDTO;
import br.com.petflow.petflow_api.service.AddressService;
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
@RequestMapping("/addresses")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Endpoints para gerenciamento de endereços")
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    @Operation(summary = "Criar um novo endereço")
    public ResponseEntity<AddressResponseDTO> create(@Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar endereço por ID")
    public ResponseEntity<AddressResponseDTO> findById(@PathVariable Long id) {
        AddressResponseDTO response = addressService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os endereços com paginação")
    public ResponseEntity<Page<AddressResponseDTO>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<AddressResponseDTO> response = addressService.findAll(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/city")
    @Operation(summary = "Buscar endereços por cidade")
    public ResponseEntity<Page<AddressResponseDTO>> findByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "city"));
        Page<AddressResponseDTO> response = addressService.findByCity(city, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/state")
    @Operation(summary = "Buscar endereços por estado")
    public ResponseEntity<Page<AddressResponseDTO>> findByState(
            @RequestParam String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "state"));
        Page<AddressResponseDTO> response = addressService.findByState(state, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tutor/{tutorId}")
    @Operation(summary = "Buscar endereços por ID do tutor")
    public ResponseEntity<Page<AddressResponseDTO>> findByTutorId(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "city"));
        Page<AddressResponseDTO> response = addressService.findByTutorId(tutorId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar endereço")
    public ResponseEntity<AddressResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO request) {
        AddressResponseDTO response = addressService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar endereço")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}