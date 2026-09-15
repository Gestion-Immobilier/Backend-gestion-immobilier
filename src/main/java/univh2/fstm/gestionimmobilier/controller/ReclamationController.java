package univh2.fstm.gestionimmobilier.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import univh2.fstm.gestionimmobilier.dto.request.ReclamationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ReclamationResponseDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;
import univh2.fstm.gestionimmobilier.service.interfaces.ReclamationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reclamations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Réclamations", description = "Gestion des réclamations locataires")
public class ReclamationController {

    private final ReclamationService reclamationService;
    private final ObjectMapper objectMapper;

    // ============================
    // CRUD de base
    // ============================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Créer une réclamation", description = "LOCATAIRE uniquement - Créer une nouvelle réclamation avec photos")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Réclamation créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    public ResponseEntity<ReclamationResponseDto> creerReclamation(
            @Valid @RequestParam("reclamation") String reclamationJson,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) {
        
        log.info("📥 POST /api/v1/reclamations - Création d'une réclamation");
        
        try {
            ReclamationRequestDto requestDto = objectMapper.readValue(reclamationJson, ReclamationRequestDto.class);
            ReclamationResponseDto response = reclamationService.creerReclamation(requestDto, photos);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("❌ Erreur parsing JSON réclamation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer une réclamation par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation trouvée"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> getReclamationById(@PathVariable Long id) {
        log.info("📥 GET /api/v1/reclamations/{}", id);
        ReclamationResponseDto response = reclamationService.getReclamationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer une réclamation par référence")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation trouvée"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> getReclamationByReference(@PathVariable String reference) {
        log.info("📥 GET /api/v1/reclamations/reference/{}", reference);
        ReclamationResponseDto response = reclamationService.getReclamationByReference(reference);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'ADMIN')")
    @Operation(summary = "Mettre à jour une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation mise à jour"),
            @ApiResponse(responseCode = "400", description = "Réclamation non modifiable"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> updateReclamation(
            @PathVariable Long id,
            @Valid @RequestBody ReclamationRequestDto requestDto) {
        
        log.info("📥 PUT /api/v1/reclamations/{}", id);
        ReclamationResponseDto response = reclamationService.updateReclamation(id, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une réclamation", description = "ADMIN uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Réclamation supprimée"),
            @ApiResponse(responseCode = "400", description = "Réclamation non supprimable"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<Void> deleteReclamation(@PathVariable Long id) {
        log.info("📥 DELETE /api/v1/reclamations/{}", id);
        reclamationService.deleteReclamation(id);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // Recherches et filtres
    // ============================

    @GetMapping
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer toutes les réclamations", description = "ADMIN et PROPRIETAIRE uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getAllReclamations() {
        log.info("📥 GET /api/v1/reclamations");
        List<ReclamationResponseDto> reclamations = reclamationService.getAllReclamations();
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer toutes les réclamations (paginé)", description = "ADMIN et PROPRIETAIRE uniquement, avec pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page récupérée")
    })
    public ResponseEntity<Page<ReclamationResponseDto>> getAllReclamationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("📥 GET /api/v1/reclamations/paged");
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ReclamationResponseDto> reclamations = reclamationService.getAllReclamationsPaged(pageable);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/mes-reclamations")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Récupérer mes réclamations", description = "LOCATAIRE uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getMesReclamations() {
        log.info("📥 GET /api/v1/reclamations/mes-reclamations");
        List<ReclamationResponseDto> reclamations = reclamationService.getMesReclamations();
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/contrat/{contratId}")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations d'un contrat")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsByContrat(@PathVariable Long contratId) {
        log.info("📥 GET /api/v1/reclamations/contrat/{}", contratId);
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsByContrat(contratId);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/locataire/{locataireId}")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations d'un locataire")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsByLocataire(@PathVariable Long locataireId) {
        log.info("📥 GET /api/v1/reclamations/locataire/{}", locataireId);
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsByLocataire(locataireId);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/bien/{bienId}")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations d'un bien")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsByBien(@PathVariable Long bienId) {
        log.info("📥 GET /api/v1/reclamations/bien/{}", bienId);
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsByBien(bienId);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/proprietaire/{proprietaireId}")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations d'un propriétaire")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsByProprietaire(@PathVariable Long proprietaireId) {
        log.info("📥 GET /api/v1/reclamations/proprietaire/{}", proprietaireId);
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsByProprietaire(proprietaireId);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/filtrer")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Filtrer les réclamations", description = "Filtrer par statut, priorité et/ou type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste filtrée récupérée")
    })
    public ResponseEntity<List<ReclamationResponseDto>> filtrerReclamations(
            @RequestParam(required = false) StatutReclamation statut,
            @RequestParam(required = false) PrioriteReclamation priorite,
            @RequestParam(required = false) TypeReclamation type) {
        
        log.info("📥 GET /api/v1/reclamations/filtrer - Statut: {}, Priorité: {}, Type: {}", statut, priorite, type);
        List<ReclamationResponseDto> reclamations = reclamationService.filtrerReclamations(statut, priorite, type);
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/rechercher")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Rechercher des réclamations", description = "Recherche par mot-clé dans titre et description")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Résultats de recherche")
    })
    public ResponseEntity<List<ReclamationResponseDto>> rechercherReclamations(
            @RequestParam String keyword) {
        
        log.info("📥 GET /api/v1/reclamations/rechercher?keyword={}", keyword);
        List<ReclamationResponseDto> reclamations = reclamationService.rechercherReclamations(keyword);
        return ResponseEntity.ok(reclamations);
    }

    // ============================
    // Gestion du statut
    // ============================

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Changer le statut d'une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut changé"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> changerStatut(
            @PathVariable Long id,
            @RequestParam StatutReclamation statut) {
        
        log.info("📥 PUT /api/v1/reclamations/{}/statut - Nouveau statut: {}", id, statut);
        ReclamationResponseDto response = reclamationService.changerStatut(id, statut);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/prendre-en-charge")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Prendre en charge une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation prise en charge"),
            @ApiResponse(responseCode = "400", description = "Réclamation déjà prise en charge"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> prendreEnCharge(
            @PathVariable Long id,
            @RequestParam(required = false) String commentaire) {
        
        log.info("📥 PUT /api/v1/reclamations/{}/prendre-en-charge", id);
        ReclamationResponseDto response = reclamationService.prendreEnCharge(id, commentaire);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/resoudre")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Résoudre une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation résolue"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> resoudreReclamation(
            @PathVariable Long id,
            @RequestParam(required = false) String solution) {
        
        log.info("📥 PUT /api/v1/reclamations/{}/resoudre", id);
        ReclamationResponseDto response = reclamationService.resoudreReclamation(id, solution);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/fermer")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Fermer une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation fermée"),
            @ApiResponse(responseCode = "400", description = "Réclamation non résolue"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> fermerReclamation(@PathVariable Long id) {
        log.info("📥 PUT /api/v1/reclamations/{}/fermer", id);
        ReclamationResponseDto response = reclamationService.fermerReclamation(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Annuler une réclamation", description = "LOCATAIRE uniquement - Annuler sa propre réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Réclamation annulée"),
            @ApiResponse(responseCode = "400", description = "Réclamation non annulable"),
            @ApiResponse(responseCode = "403", description = "Pas votre réclamation"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<Void> annulerReclamation(@PathVariable Long id) {
        log.info("📥 PUT /api/v1/reclamations/{}/annuler", id);
        reclamationService.annulerReclamation(id);
        return ResponseEntity.ok().build();
    }

    // ============================
    // Gestion des photos
    // ============================

    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'ADMIN')")
    @Operation(summary = "Ajouter des photos à une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Photos ajoutées"),
            @ApiResponse(responseCode = "400", description = "Nombre maximum de photos atteint"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<ReclamationResponseDto> ajouterPhotos(
            @PathVariable Long id,
            @RequestParam("photos") List<MultipartFile> photos) {
        
        log.info("📥 POST /api/v1/reclamations/{}/photos - {} photos", id, photos.size());
        ReclamationResponseDto response = reclamationService.ajouterPhotos(id, photos);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/photos")
    @PreAuthorize("hasAnyRole('LOCATAIRE', 'ADMIN')")
    @Operation(summary = "Supprimer une photo d'une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Photo supprimée"),
            @ApiResponse(responseCode = "400", description = "Photo non trouvée"),
            @ApiResponse(responseCode = "404", description = "Réclamation non trouvée")
    })
    public ResponseEntity<Void> supprimerPhoto(
            @PathVariable Long id,
            @RequestParam String photoUrl) {
        
        log.info("📥 DELETE /api/v1/reclamations/{}/photos", id);
        reclamationService.supprimerPhoto(id, photoUrl);
        return ResponseEntity.noContent().build();
    }

    // ============================
    // Réclamations spéciales
    // ============================

    @GetMapping("/urgentes")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations urgentes non traitées")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réclamations urgentes")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsUrgentes() {
        log.info("📥 GET /api/v1/reclamations/urgentes");
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsUrgentes();
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/en-retard")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations en retard", description = "Réclamations non traitées depuis plus de 48h")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réclamations en retard")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsEnRetard() {
        log.info("📥 GET /api/v1/reclamations/en-retard");
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsEnRetard();
        return ResponseEntity.ok(reclamations);
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Récupérer les réclamations récentes", description = "Réclamations des dernières 24h")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des réclamations récentes")
    })
    public ResponseEntity<List<ReclamationResponseDto>> getReclamationsRecentes() {
        log.info("📥 GET /api/v1/reclamations/recentes");
        List<ReclamationResponseDto> reclamations = reclamationService.getReclamationsRecentes();
        return ResponseEntity.ok(reclamations);
    }

    // ============================
    // Statistiques
    // ============================

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Nombre total de réclamations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre total")
    })
    public ResponseEntity<Long> getTotalReclamations() {
        log.info("📥 GET /api/v1/reclamations/stats/total");
        long total = reclamationService.getTotalReclamations();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/statut/{statut}")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Nombre de réclamations par statut")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nombre par statut")
    })
    public ResponseEntity<Long> countByStatut(@PathVariable StatutReclamation statut) {
        log.info("📥 GET /api/v1/reclamations/stats/statut/{}", statut);
        long count = reclamationService.countByStatut(statut);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/stats/par-statut")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Répartition des réclamations par statut")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiques par statut")
    })
    public ResponseEntity<Map<StatutReclamation, Long>> getStatistiquesParStatut() {
        log.info("📥 GET /api/v1/reclamations/stats/par-statut");
        Map<StatutReclamation, Long> stats = reclamationService.getStatistiquesParStatut();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/par-type")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Répartition des réclamations par type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiques par type")
    })
    public ResponseEntity<Map<TypeReclamation, Long>> getStatistiquesParType() {
        log.info("📥 GET /api/v1/reclamations/stats/par-type");
        Map<TypeReclamation, Long> stats = reclamationService.getStatistiquesParType();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/par-priorite")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Répartition des réclamations par priorité")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistiques par priorité")
    })
    public ResponseEntity<Map<PrioriteReclamation, Long>> getStatistiquesParPriorite() {
        log.info("📥 GET /api/v1/reclamations/stats/par-priorite");
        Map<PrioriteReclamation, Long> stats = reclamationService.getStatistiquesParPriorite();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/delai-moyen")
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Délai moyen de résolution", description = "Nombre de jours moyen pour résoudre une réclamation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Délai moyen en jours")
    })
    public ResponseEntity<Double> getDelaiResolutionMoyen() {
        log.info("📥 GET /api/v1/reclamations/stats/delai-moyen");
        Double delai = reclamationService.getDelaiResolutionMoyen();
        return ResponseEntity.ok(delai);
    }
}