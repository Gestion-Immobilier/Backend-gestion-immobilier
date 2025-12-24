package univh2.fstm.gestionimmobilier.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.request.DemandeResiliationRequestDto;
import univh2.fstm.gestionimmobilier.dto.request.TraiterDemandeResiliationDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeResiliationResponseDto;
import univh2.fstm.gestionimmobilier.model.StatutDemandeResiliation;
import univh2.fstm.gestionimmobilier.service.interfaces.DemandeResiliationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/demandes-resiliation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Demandes de Résiliation", description = "API de gestion des demandes de résiliation")
public class DemandeResiliationController {
    private final DemandeResiliationService demandeResiliationService;

    // ========================================
    // ENDPOINTS LOCATAIRE
    // ========================================

    @PostMapping
    @PreAuthorize("hasAnyRole('LOCATAIRE','PROPRIETAIRE')")
    @Operation(summary = "Créer une demande de résiliation", description = "LOCATAIRE | PROPRIETAIRE - Créer une nouvelle demande de résiliation pour un contrat actif")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Demande créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou contrat non éligible"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    public ResponseEntity<DemandeResiliationResponseDto> creerDemande(
            @Valid @RequestBody DemandeResiliationRequestDto requestDto) {

        log.info("📥 POST /api/v1/demandes-resiliation - Création d'une demande");
        DemandeResiliationResponseDto response = demandeResiliationService.creerDemande(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Récupérer mes demandes de résiliation", description = "LOCATAIRE uniquement - Liste de toutes mes demandes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    public ResponseEntity<List<DemandeResiliationResponseDto>> getMesDemandesResiliation() {
        log.info("📥 GET /api/v1/demandes-resiliation/mes-demandes");
        List<DemandeResiliationResponseDto> demandes = demandeResiliationService.getMesDemandesResiliation();
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer une demande par ID", description = "LOCATAIRE (ses demandes) ou ADMIN (toutes)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès non autorisé"),
            @ApiResponse(responseCode = "404", description = "Demande non trouvée")
    })
    public ResponseEntity<DemandeResiliationResponseDto> getDemandeById(@PathVariable Long id) {
        log.info("📥 GET /api/v1/demandes-resiliation/{}", id);
        DemandeResiliationResponseDto demande = demandeResiliationService.getDemandeById(id);
        return ResponseEntity.ok(demande);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Annuler une demande", description = "LOCATAIRE uniquement - Annuler une demande en attente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande annulée avec succès"),
            @ApiResponse(responseCode = "400", description = "Demande déjà traitée, ne peut pas être annulée"),
            @ApiResponse(responseCode = "403", description = "Cette demande ne vous appartient pas"),
            @ApiResponse(responseCode = "404", description = "Demande non trouvée")
    })
    public ResponseEntity<Void> annulerDemande(@PathVariable Long id) {
        log.info("📥 PUT /api/v1/demandes-resiliation/{}/annuler", id);
        demandeResiliationService.annulerDemande(id);
        return ResponseEntity.ok().build();
    }



    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer toutes les demandes", description = "ADMIN uniquement - Liste paginée de toutes les demandes")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    public ResponseEntity<List<DemandeResiliationResponseDto>> getAllDemandes() {

        log.info("📥 GET /api/v1/demandes-resiliation");


        List<DemandeResiliationResponseDto> demandes = demandeResiliationService.getAllDemandes();
        return ResponseEntity.ok(demandes);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer les demandes par statut", description = "ADMIN uniquement - Filtrer par statut avec pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "400", description = "Statut invalide")
    })
    public ResponseEntity<List<DemandeResiliationResponseDto>> getDemandesByStatut(
            @PathVariable StatutDemandeResiliation statut) {

        log.info("📥 GET /api/v1/demandes-resiliation/statut/{}", statut);

        List<DemandeResiliationResponseDto> demandes = demandeResiliationService.getDemandesByStatut(statut);
        return ResponseEntity.ok(demandes);
    }

    @PutMapping("/{id}/traiter")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Traiter une demande", description = "ADMIN uniquement - Accepter ou refuser une demande de résiliation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demande traitée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou demande déjà traitée"),
            @ApiResponse(responseCode = "404", description = "Demande non trouvée")
    })
    public ResponseEntity<DemandeResiliationResponseDto> traiterDemande(
            @PathVariable Long id,
            @Valid @RequestBody TraiterDemandeResiliationDto traiterDto) {

        log.info("📥 PUT /api/v1/demandes-resiliation/{}/traiter - Statut: {}", id, traiterDto.getStatut());
        DemandeResiliationResponseDto response = demandeResiliationService.traiterDemande(id, traiterDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Compter les demandes en attente", description = "ADMIN uniquement - Nombre de demandes EN_ATTENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comptage effectué")
    })
    public ResponseEntity<Long> countDemandesEnAttente() {
        log.info("📥 GET /api/v1/demandes-resiliation/stats/en-attente");
        long count = demandeResiliationService.countDemandesEnAttente();
        return ResponseEntity.ok(count);
    }

}
