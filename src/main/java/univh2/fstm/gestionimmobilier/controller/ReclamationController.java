package univh2.fstm.gestionimmobilier.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.ReclamationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ApiResponseDto;
import univh2.fstm.gestionimmobilier.dto.response.ReclamationResponseDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;
import univh2.fstm.gestionimmobilier.service.interfaces.ReclamationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reclamations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Réclamations", description = "API de gestion des réclamations")
public class ReclamationController {

    private final ReclamationService reclamationService;

    // ============================
    // CRUD de base
    // ============================

    @Operation(
        summary = "Créer une nouvelle réclamation",
        description = "Permet à un locataire de créer une nouvelle réclamation"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Réclamation créée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides ou limite de photos dépassée"),
        @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'AGENT')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> creerReclamation(
            @RequestPart("reclamation") @Valid ReclamationRequestDto requestDto,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        
        log.info("📝 Demande création réclamation - Type: {}, Contrat: {}", 
                requestDto.getTypeReclamation(), requestDto.getContratId());
        
        ReclamationResponseDto response = reclamationService.creerReclamation(requestDto, photos);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation créée avec succès")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @Operation(summary = "Récupérer une réclamation par ID", description = "Récupère les détails d'une réclamation spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Réclamation trouvée"),
        @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> getReclamationById(
            @Parameter(description = "ID de la réclamation", required = true) @PathVariable Long id) {
        
        ReclamationResponseDto response = reclamationService.getReclamationById(id);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation récupérée avec succès")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Récupérer une réclamation par référence", description = "Récupère une réclamation par sa référence unique")
    @GetMapping("/reference/{reference}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> getReclamationByReference(@PathVariable String reference) {
        
        ReclamationResponseDto response = reclamationService.getReclamationByReference(reference);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation récupérée avec succès")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Mettre à jour une réclamation", description = "Met à jour les informations d'une réclamation existante")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'AGENT')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> updateReclamation(
            @PathVariable Long id,
            @RequestBody @Valid ReclamationRequestDto requestDto) {
        
        log.info("✏️ Mise à jour réclamation ID: {}", id);
        
        ReclamationResponseDto response = reclamationService.updateReclamation(id, requestDto);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation mise à jour avec succès")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Supprimer une réclamation", description = "Supprime une réclamation")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> deleteReclamation(@PathVariable Long id) {
        
        log.info("🗑️ Suppression réclamation ID: {}", id);
        
        reclamationService.deleteReclamation(id);
        
        ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Réclamation supprimée avec succès")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ============================
    // Recherches et filtres
    // ============================

    @Operation(summary = "Récupérer toutes les réclamations", description = "Récupère la liste de toutes les réclamations")
    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getAllReclamations() {
        
        List<ReclamationResponseDto> reclamations = reclamationService.getAllReclamations();
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(reclamations.size() + " réclamation(s) trouvée(s)")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Récupérer les réclamations d'un contrat", description = "Liste les réclamations d'un contrat spécifique")
    @GetMapping("/contrat/{contratId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getReclamationsByContrat(@PathVariable Long contratId) {
        
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsByContrat(contratId);
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(reclamations.size() + " réclamation(s) pour ce contrat")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Récupérer les réclamations d'un locataire", description = "Liste les réclamations d'un locataire")
    @GetMapping("/locataire/{locataireId}")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getReclamationsByLocataire(@PathVariable Long locataireId) {
        
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsByLocataire(locataireId);
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(reclamations.size() + " réclamation(s) pour ce locataire")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Mes réclamations", description = "Récupère les réclamations de l'utilisateur connecté (locataire)")
    @GetMapping("/mes-reclamations")
    @PreAuthorize("hasRole('LOCATAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getMesReclamations() {
        
        List<ReclamationResponseDto> reclamations = reclamationService.getMesReclamations();
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message("Vous avez " + reclamations.size() + " réclamation(s)")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Filtrer les réclamations", description = "Filtre les réclamations par critères")
    @GetMapping("/filtrer")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> filtrerReclamations(
            @RequestParam(required = false) StatutReclamation statut,
            @RequestParam(required = false) PrioriteReclamation priorite,
            @RequestParam(required = false) TypeReclamation type) {
        
        List<ReclamationResponseDto> reclamations = reclamationService.filtrerReclamations(statut, priorite, type);
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(reclamations.size() + " réclamation(s) correspondant aux critères")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Rechercher des réclamations", description = "Recherche des réclamations par mot-clé")
    @GetMapping("/rechercher")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> rechercherReclamations(@RequestParam String keyword) {
        
        List<ReclamationResponseDto> reclamations = reclamationService.rechercherReclamations(keyword);
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(reclamations.size() + " résultat(s) pour '" + keyword + "'")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ============================
    // Gestion du statut
    // ============================

    @Operation(summary = "Changer le statut d'une réclamation", description = "Met à jour le statut d'une réclamation")
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutReclamation nouveauStatut) {
        
        log.info("🔄 Changement statut réclamation {} → {}", id, nouveauStatut);
        
        ReclamationResponseDto response = reclamationService.changerStatut(id, nouveauStatut);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Statut changé à: " + nouveauStatut)
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Prendre en charge une réclamation", description = "Marque une réclamation comme prise en charge")
    @PostMapping("/{id}/prendre-en-charge")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> prendreEnCharge(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire) {
        
        ReclamationResponseDto response = reclamationService.prendreEnCharge(id, commentaire);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation prise en charge")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Résoudre une réclamation", description = "Marque une réclamation comme résolue")
    @PostMapping("/{id}/resoudre")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> resoudreReclamation(
            @PathVariable Long id,
            @RequestParam(required = false) String solution) {
        
        ReclamationResponseDto response = reclamationService.resoudreReclamation(id, solution);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation marquée comme résolue")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Fermer une réclamation", description = "Ferme une réclamation résolue")
    @PostMapping("/{id}/fermer")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> fermerReclamation(@PathVariable Long id) {
        
        ReclamationResponseDto response = reclamationService.fermerReclamation(id);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation fermée")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Annuler une réclamation", description = "Permet à un locataire d'annuler sa réclamation")
    @PostMapping("/{id}/annuler")
    @PreAuthorize("hasRole('LOCATAIRE')")
    public ResponseEntity<ApiResponseDto<Void>> annulerReclamation(@PathVariable Long id) {
        
        reclamationService.annulerReclamation(id);
        
        ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Réclamation annulée")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ============================
    // Gestion des photos
    // ============================

    @Operation(summary = "Ajouter des photos à une réclamation", description = "Ajoute des photos à une réclamation existante")
    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'AGENT')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> ajouterPhotos(
            @PathVariable Long id,
            @RequestPart("photos") List<MultipartFile> photos) {
        
        ReclamationResponseDto response = reclamationService.ajouterPhotos(id, photos);
        
        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message(photos.size() + " photo(s) ajoutée(s)")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Supprimer une photo", description = "Supprime une photo d'une réclamation")
    @DeleteMapping("/{reclamationId}/photos")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> supprimerPhoto(
            @PathVariable Long reclamationId,
            @RequestParam String photoUrl) {
        
        reclamationService.supprimerPhoto(reclamationId, photoUrl);
        
        ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Photo supprimée")
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ============================
    // Réclamations spéciales
    // ============================

    @Operation(summary = "Récupérer les réclamations urgentes", description = "Récupère les réclamations urgentes non traitées")
    @GetMapping("/urgentes")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getReclamationsUrgentes() {
        
        List<ReclamationResponseDto> urgentes = reclamationService.getReclamationsUrgentes();
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(urgentes.size() + " réclamation(s) urgente(s)")
                .data(urgentes)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Récupérer les réclamations en retard", description = "Récupère les réclamations en retard (> 48h)")
    @GetMapping("/en-retard")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getReclamationsEnRetard() {
        
        List<ReclamationResponseDto> enRetard = reclamationService.getReclamationsEnRetard();
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(enRetard.size() + " réclamation(s) en retard")
                .data(enRetard)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Récupérer les réclamations récentes", description = "Récupère les réclamations des dernières 24h")
    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getReclamationsRecentes() {
        
        List<ReclamationResponseDto> recentes = reclamationService.getReclamationsRecentes();
        
        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(recentes.size() + " réclamation(s) récente(s)")
                .data(recentes)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    // ============================
    // Statistiques
    // ============================

    @Operation(summary = "Statistiques générales", description = "Récupère les statistiques globales")
    @GetMapping("/statistiques")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> getStatistiques() {
        
        long total = reclamationService.getTotalReclamations();
        Map<StatutReclamation, Long> parStatut = reclamationService.getStatistiquesParStatut();
        Map<TypeReclamation, Long> parType = reclamationService.getStatistiquesParType();
        Map<PrioriteReclamation, Long> parPriorite = reclamationService.getStatistiquesParPriorite();
        Double delaiMoyen = reclamationService.getDelaiResolutionMoyen();
        
        Map<String, Object> stats = Map.of(
            "total", total,
            "parStatut", parStatut,
            "parType", parType,
            "parPriorite", parPriorite,
            "delaiResolutionMoyenJours", delaiMoyen
        );
        
        ApiResponseDto<Map<String, Object>> apiResponse = ApiResponseDto.<Map<String, Object>>builder()
                .success(true)
                .message("Statistiques des réclamations")
                .data(stats)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Statistiques par statut", description = "Récupère le nombre de réclamations par statut")
    @GetMapping("/statistiques/statut")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<Map<StatutReclamation, Long>>> getStatistiquesParStatut() {
        
        Map<StatutReclamation, Long> stats = reclamationService.getStatistiquesParStatut();
        
        ApiResponseDto<Map<StatutReclamation, Long>> apiResponse = ApiResponseDto.<Map<StatutReclamation, Long>>builder()
                .success(true)
                .message("Statistiques par statut")
                .data(stats)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Délai moyen de résolution", description = "Calcule le délai moyen de résolution")
    @GetMapping("/statistiques/delai-moyen")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<Double>> getDelaiResolutionMoyen() {
        
        Double delaiMoyen = reclamationService.getDelaiResolutionMoyen();
        
        ApiResponseDto<Double> apiResponse = ApiResponseDto.<Double>builder()
                .success(true)
                .message("Délai moyen de résolution")
                .data(delaiMoyen)
                .timestamp(LocalDateTime.now())
                .build();
        
        return ResponseEntity.ok(apiResponse);
    }
    // AJOUTE CETTE MÉTHODE dans ReclamationController.java :
    @Operation(
            summary = "Créer une réclamation (JSON simple)",
            description = "Version simplifiée pour les tests sans photos"
    )
    @PostMapping("/simple")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'AGENT')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> creerReclamationSimple(
            @RequestBody @Valid ReclamationRequestDto requestDto) {

        log.info("📝 Demande création réclamation simple - Type: {}, Contrat: {}",
                requestDto.getTypeReclamation(), requestDto.getContratId());

        // Appeler le service sans photos
        ReclamationResponseDto response = reclamationService.creerReclamation(requestDto, null);

        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réclamation créée avec succès")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    // AJOUTER DANS ReclamationController.java
    @Operation(summary = "Répondre à une réclamation", description = "Permet à l'admin de répondre et changer le statut")
    @PostMapping("/{id}/repondre")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<ReclamationResponseDto>> repondreReclamation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String message = request.get("message");
        String nouveauStatut = request.get("nouveauStatut");
        String solution = request.get("solution");

        ReclamationResponseDto response = reclamationService.repondreReclamation(
                id, message, nouveauStatut, solution
        );

        ApiResponseDto<ReclamationResponseDto> apiResponse = ApiResponseDto.<ReclamationResponseDto>builder()
                .success(true)
                .message("Réponse envoyée avec succès")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Notifier le locataire", description = "Envoie une notification au locataire")
    @PostMapping("/{id}/notifier")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<Void>> notifierLocataire(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String message = request.get("message");
        reclamationService.notifierLocataire(id, message);

        ApiResponseDto<Void> apiResponse = ApiResponseDto.<Void>builder()
                .success(true)
                .message("Notification envoyée")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Réclamations avec détails", description = "Récupère les réclamations avec toutes les informations")
    @GetMapping("/avec-details")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN', 'PROPRIETAIRE')")
    public ResponseEntity<ApiResponseDto<List<ReclamationResponseDto>>> getReclamationsAvecDetails() {

        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsAvecDetails();

        ApiResponseDto<List<ReclamationResponseDto>> apiResponse = ApiResponseDto.<List<ReclamationResponseDto>>builder()
                .success(true)
                .message(reclamations.size() + " réclamation(s) avec détails")
                .data(reclamations)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}