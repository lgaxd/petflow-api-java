package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.AddressRequestDTO;
import br.com.petflow.petflow_api.dto.AddressResponseDTO;
import br.com.petflow.petflow_api.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "Listar todos os endereços")
    public ResponseEntity<List<AddressResponseDTO>> findAll() {
        List<AddressResponseDTO> response = addressService.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/city")
    @Operation(summary = "Buscar endereços por cidade")
    public ResponseEntity<List<AddressResponseDTO>> findByCity(@RequestParam String city) {
        List<AddressResponseDTO> response = addressService.findByCity(city);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/state")
    @Operation(summary = "Buscar endereços por estado")
    public ResponseEntity<List<AddressResponseDTO>> findByState(@RequestParam String state) {
        List<AddressResponseDTO> response = addressService.findByState(state);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-tutor/{tutorId}")
    @Operation(summary = "Buscar endereços por ID do tutor")
    public ResponseEntity<List<AddressResponseDTO>> findByTutorId(@PathVariable Long tutorId) {
        List<AddressResponseDTO> response = addressService.findByTutorId(tutorId);
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