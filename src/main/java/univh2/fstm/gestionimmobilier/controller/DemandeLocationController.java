package univh2.fstm.gestionimmobilier.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationRequestDto;
import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationTraitementDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeLocationResponseDto;
import univh2.fstm.gestionimmobilier.model.StatutDemande;
import univh2.fstm.gestionimmobilier.service.interfaces.DemandeLocationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/demandes-location")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Demandes Location", description = "API de gestion des demandes de location")
public class DemandeLocationController {

    private final DemandeLocationService demandeService;

    // ========================================
    // CRÉATION ET CONSULTATION
    // ========================================

    @PostMapping
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Créer une demande de location", description = "Le locataire demande à louer un bien")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Demande créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<DemandeLocationResponseDto> creerDemande(
            @Valid @RequestBody DemandeLocationRequestDto requestDto) {

        log.info("📥 POST /api/v1/demandes-location");
        DemandeLocationResponseDto response = demandeService.creerDemande(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @demandeLocationSecurityService.isProprietaireOrLocataireDeLaDemande(#id)")
    @Operation(summary = "Récupérer une demande par ID")
    public ResponseEntity<DemandeLocationResponseDto> getDemandeById(@PathVariable Long id) {
        log.info("📥 GET /api/v1/demandes-location/{}", id);
        DemandeLocationResponseDto response = demandeService.getDemandeById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer toutes les demandes", description = "ADMIN uniquement")
    public ResponseEntity<List<DemandeLocationResponseDto>> getAllDemandes() {
        log.info("📥 GET /api/v1/demandes-location");
        List<DemandeLocationResponseDto> demandes = demandeService.getAllDemandes();
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Mes demandes", description = "Demandes du locataire connecté")
    public ResponseEntity<List<DemandeLocationResponseDto>> getMesDemandes() {
        log.info("📥 GET /api/v1/demandes-location/mes-demandes");

        // TODO: Récupérer l'ID du locataire connecté via SecurityContext
        Long locataireId = null;  // À implémenter

        List<DemandeLocationResponseDto> demandes = demandeService.getMesDemandes(locataireId);
        return ResponseEntity.ok(demandes);
    }

    // ========================================
    // TRAITEMENT (ADMIN)
    // ========================================

    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Demandes en attente", description = "Pour validation par ADMIN")
    public ResponseEntity<List<DemandeLocationResponseDto>> getDemandesEnAttente() {
        log.info("📥 GET /api/v1/demandes-location/en-attente");
        List<DemandeLocationResponseDto> demandes = demandeService.getDemandesEnAttente();
        return ResponseEntity.ok(demandes);
    }

    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Accepter une demande", description = "ADMIN uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande acceptée"),
            @ApiResponse(responseCode = "400", description = "Demande déjà traitée"),
            @ApiResponse(responseCode = "404", description = "Demande introuvable")
    })
    public ResponseEntity<DemandeLocationResponseDto> accepterDemande(@PathVariable Long id) {
        log.info("📥 PATCH /api/v1/demandes-location/{}/accepter", id);
        DemandeLocationResponseDto response = demandeService.accepterDemande(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refuser une demande", description = "ADMIN uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande refusée"),
            @ApiResponse(responseCode = "400", description = "Motif requis ou demande déjà traitée"),
            @ApiResponse(responseCode = "404", description = "Demande introuvable")
    })
    public ResponseEntity<DemandeLocationResponseDto> refuserDemande(
            @PathVariable Long id,
            @Valid @RequestBody DemandeLocationTraitementDto traitementDto) {

        log.info("📥 PATCH /api/v1/demandes-location/{}/refuser", id);
        DemandeLocationResponseDto response = demandeService.refuserDemande(id, traitementDto);
        return ResponseEntity.ok(response);
    }

    // ========================================
    // STATISTIQUES
    // ========================================

    @GetMapping("/stats/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Nombre de demandes en attente")
    public ResponseEntity<Map<String, Long>> compterDemandesEnAttente() {
        log.info("📥 GET /api/v1/demandes-location/stats/en-attente");
        long count = demandeService.compterDemandesEnAttente();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Demandes par statut")
    public ResponseEntity<List<DemandeLocationResponseDto>> getDemandesByStatut(
            @PathVariable StatutDemande statut) {

        log.info("📥 GET /api/v1/demandes-location/statut/{}", statut);
        List<DemandeLocationResponseDto> demandes = demandeService.getDemandesByStatut(statut);
        return ResponseEntity.ok(demandes);
    }
}