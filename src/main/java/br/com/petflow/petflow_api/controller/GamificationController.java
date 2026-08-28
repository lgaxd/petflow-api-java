package br.com.petflow.petflow_api.controller;

import br.com.petflow.petflow_api.dto.*;
import br.com.petflow.petflow_api.service.GamificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Tag(name = "Gamificação", description = "Endpoints para gamificação, pontos e recompensas")
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping("/points")
    @Operation(summary = "Buscar pontos do tutor autenticado")
    public ResponseEntity<TutorPointsDTO> getMyPoints(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long tutorId = extractTutorId(userDetails);
        return ResponseEntity.ok(gamificationService.getTutorPoints(tutorId));
    }

    @GetMapping("/pets/{petId}/risk")
    @Operation(summary = "Buscar score de risco de um pet")
    public ResponseEntity<PetRiskDTO> getPetRisk(@PathVariable Long petId) {
        return ResponseEntity.ok(gamificationService.getPetRisk(petId));
    }

    @GetMapping("/coupons/available")
    @Operation(summary = "Listar cupons disponíveis para resgate")
    public ResponseEntity<Page<CouponCatalogDTO>> getAvailableCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("pointsRequired").ascending());
        return ResponseEntity.ok(gamificationService.getAvailableCoupons(pageable));
    }

    @PostMapping("/redeem")
    @Operation(summary = "Resgatar um cupom usando pontos")
    public ResponseEntity<RedeemResponseDTO> redeemCoupon(
            Authentication authentication,
            @Valid @RequestBody RedeemCouponRequestDTO request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long tutorId = extractTutorId(userDetails);
        return ResponseEntity.ok(gamificationService.redeemCoupon(tutorId, request.getCouponId()));
    }

    private Long extractTutorId(UserDetails userDetails) {
        // Pega o ID do Tutor do UserDetails
        // O email é o username, então buscamos o tutor pelo email
        // Mas na implementação atual, o Tutor já implementa UserDetails
        // e está disponível no principal
        return ((br.com.petflow.petflow_api.entity.Tutor) userDetails).getId();
    }
}