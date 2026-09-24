package univh2.fstm.gestionimmobilier.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
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
import univh2.fstm.gestionimmobilier.dto.request.BienRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.BienResponseDto;
import univh2.fstm.gestionimmobilier.dto.request.BienValidationDto;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;
import univh2.fstm.gestionimmobilier.model.TypeBien;
import univh2.fstm.gestionimmobilier.service.interfaces.BienService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Biens", description = "API de gestion des biens immobiliers")
@RequestMapping("/api/v1/biens")
public class BienController {
    private final BienService bienService;

    // crud basique

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('PROPRIETAIRE', 'ADMIN')")
    @Operation(summary = "Créer un nouveau bien", description = "Crée un bien immobilier (Propriétaire)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bien créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<BienResponseDto> creerBien(@Parameter(description = "Données du bien au format JSON", required = true)
                                                         @RequestPart("bien") String bienJson,
                                                     @Parameter(description = "Photos du bien", required = false)
                                                         @RequestPart(value = "photos", required = false) List<MultipartFile> photos){
        log.info("POST /api/v1/biens - Creation d'un bien");
        try {
            // Désérialiser le JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            BienRequestDto requestDto = objectMapper.readValue(bienJson, BienRequestDto.class);

            log.info("✅ JSON parsé avec succès pour le bien: {}", requestDto.getTypeBien());

            // Valider le DTO
            validateBienRequest(requestDto);

        BienResponseDto responseDto = bienService.creerBien(requestDto,photos);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
        } catch (JsonProcessingException e) {
            log.error("❌ Erreur de parsing JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ConstraintViolationException e) {
            log.error("❌ Validation échouée: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }


    private void validateBienRequest(BienRequestDto dto) {
        Set<ConstraintViolation<BienRequestDto>> violations =
                Validation.buildDefaultValidatorFactory().getValidator().validate(dto);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }





    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer un bien par ID", description = "Récupère les détails d'un bien")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bien trouvé"),
            @ApiResponse(responseCode = "404", description = "Bien introuvable")
    })
    public ResponseEntity<BienResponseDto> getBienById(@Parameter(description = "ID du bien") @PathVariable Long id){
        log.info("GET /api/v1/biens/{}",id);
        BienResponseDto responseDto = bienService.getBienById(id);
        return ResponseEntity.ok(responseDto);
    }



    @GetMapping("/reference/{reference}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Récupérer un bien par référence")
    public ResponseEntity<BienResponseDto> getBienByReference(
            @Parameter(description = "Référence du bien") @PathVariable String reference) {

        log.info("📥 GET /api/v1/biens/reference/{}", reference);
        BienResponseDto response = bienService.getBienByReference(reference);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer tous les biens", description = "Liste complète (AGENT/ADMIN uniquement)")
    public ResponseEntity<List<BienResponseDto>> getAllBiens() {
        log.info("📥 GET /api/v1/biens - Récupération de tous les biens");
        List<BienResponseDto> biens = bienService.getAllBiens();
        return ResponseEntity.ok(biens);
    }


    @GetMapping("/proprietaire/{proprietaireId}")
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaire(#proprietaireId)")
    @Operation(summary = "Récupérer les biens d'un propriétaire")
    public ResponseEntity<List<BienResponseDto>> getBiensByProprietaire(
            @PathVariable Long proprietaireId) {

        log.info("📥 GET /api/v1/biens/proprietaire/{}", proprietaireId);
        List<BienResponseDto> biens = bienService.getBiensByProprietaire(proprietaireId);
        return ResponseEntity.ok(biens);
    }


    @PutMapping(value="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaireDuBien(#id)")
    @Operation(summary = "Mettre à jour un bien", description = "Modifie un bien existant")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bien mis à jour"),
            @ApiResponse(responseCode = "404", description = "Bien introuvable"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<BienResponseDto> updateBien(@Parameter(description = "ID du bien à mettre à jour", required = true)
                                                          @PathVariable Long id,
                                                      @Parameter(description = "Données du bien au format JSON", required = true)
                                                          @RequestPart("bien") String bienJson,
                                                      @Parameter(description = "Photos à ajouter au bien", required = false)
                                                          @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {

        log.info("📥 PUT /api/v1/biens/{}", id);
        try {
            // Désérialiser le JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            BienRequestDto requestDto = objectMapper.readValue(bienJson, BienRequestDto.class);

            log.info("✅ JSON parsé avec succès pour le bien: {}", requestDto.getTypeBien());

            // Valider le DTO
            validateBienRequest(requestDto);

            // Appeler le service pour mettre à jour le bien avec photos
            BienResponseDto response = bienService.updateBien(id, requestDto, photos);
            return ResponseEntity.ok(response);

        } catch (JsonProcessingException e) {
            log.error("❌ Erreur de parsing JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ConstraintViolationException e) {
            log.error("❌ Validation échouée: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }



    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Changer le statut d'un bien", description = "Modifie DISPONIBLE/LOUE/EN_MAINTENANCE")
    public ResponseEntity<BienResponseDto> changerStatutBien(
            @PathVariable Long id,
            @RequestParam StatutBien statut) {

        log.info("📥 PATCH /api/v1/biens/{}/statut - Nouveau statut: {}", id, statut);
        BienResponseDto response = bienService.changerStatutBien(id, statut);
        return ResponseEntity.ok(response);
    }




    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaireDuBien(#id)")
    @Operation(summary = "Supprimer un bien")
    @ApiResponse(responseCode = "204", description = "Bien supprimé")
    public ResponseEntity<Void> deleteBien(@PathVariable Long id) {
        log.info("📥 DELETE /api/v1/biens/{}", id);
        bienService.deleteBien(id);
        return ResponseEntity.noContent().build();
    }




    // GESTION VALIDATION (AGENT)
    @GetMapping("/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Biens en attente de validation", description = "Liste pour AGENT uniquement")
    public ResponseEntity<List<BienResponseDto>> getBiensEnAttente() {
        log.info("📥 GET /api/v1/biens/en-attente");
        List<BienResponseDto> biens = bienService.getBiensEnAttente();
        return ResponseEntity.ok(biens);
    }



    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Valider ou rejeter un bien", description = "AGENT seulement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bien validé/rejeté"),
            @ApiResponse(responseCode = "400", description = "Validation invalide"),
            @ApiResponse(responseCode = "404", description = "Bien introuvable")
    })
    public ResponseEntity<BienResponseDto> validerBien(
            @PathVariable Long id,
            @Valid @RequestBody BienValidationDto validationDto) {

        log.info("📥 PATCH /api/v1/biens/{}/valider - Statut: {}",
                id, validationDto.getStatutValidation());
        BienResponseDto response = bienService.validerBien(id, validationDto);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/stats/en-attente")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Nombre de biens en attente")
    public ResponseEntity<Map<String, Long>> compterBiensEnAttente() {
        log.info("📥 GET /api/v1/biens/stats/en-attente");
        long count = bienService.compterBiensEnAttente();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }



    // RECHERCHE PUBLIQUE (CLIENTS)

    @GetMapping("/publics")
    @Operation(summary = "Biens publics", description = "Biens validés et disponibles (CLIENTS)")
    public ResponseEntity<List<BienResponseDto>> getBiensPublics() {
        log.info("📥 GET /api/v1/biens/publics");
        List<BienResponseDto> biens = bienService.getBiensPublics();
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/publics/paged")
    @Operation(summary = "Biens publics (paginé)", description = "Biens validés et disponibles avec pagination")
    public ResponseEntity<Page<BienResponseDto>> getBiensPublicsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/biens/publics/paged");
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BienResponseDto> biens = bienService.getBiensPublicsPagines(pageable);
        return ResponseEntity.ok(biens);
    }



    @GetMapping("/recherche/ville")
    @Operation(summary = "Rechercher par ville")
    public ResponseEntity<List<BienResponseDto>> rechercherParVille(
            @RequestParam String ville) {

        log.info("📥 GET /api/v1/biens/recherche/ville?ville={}", ville);
        List<BienResponseDto> biens = bienService.rechercherParVille(ville);
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/recherche/ville/paged")
    @Operation(summary = "Rechercher par ville (paginé)")
    public ResponseEntity<Page<BienResponseDto>> rechercherParVillePaged(
            @RequestParam String ville,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("📥 GET /api/v1/biens/recherche/ville/paged?ville={}", ville);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BienResponseDto> biens = bienService.rechercherParVillePaginee(ville, pageable);
        return ResponseEntity.ok(biens);
    }



    @GetMapping("/recherche/type")
    @Operation(summary = "Rechercher par type de bien")
    public ResponseEntity<List<BienResponseDto>> rechercherParType(
            @RequestParam TypeBien typeBien) {

        log.info("📥 GET /api/v1/biens/recherche/type?typeBien={}", typeBien);
        List<BienResponseDto> biens = bienService.rechercherParType(typeBien);
        return ResponseEntity.ok(biens);
    }



    @GetMapping("/recherche/avancee")
    @Operation(summary = "Recherche avancée", description = "Filtres multiples")
    public ResponseEntity<List<BienResponseDto>> rechercheAvancee(
            @Parameter(description = "Ville (optionnel)")
            @RequestParam(required = false) String ville,

            @Parameter(description = "Type de bien (optionnel)")
            @RequestParam(required = false) TypeBien typeBien,

            @Parameter(description = "Prix minimum (optionnel)")
            @RequestParam(required = false) BigDecimal prixMin,

            @Parameter(description = "Prix maximum (optionnel)")
            @RequestParam(required = false) BigDecimal prixMax) {

        log.info("📥 GET /api/v1/biens/recherche/avancee - Filtres: ville={}, type={}, prix={}-{}",
                ville, typeBien, prixMin, prixMax);

        List<BienResponseDto> biens = bienService.rechercheAvancee(ville, typeBien, prixMin, prixMax);
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/recherche/avancee/paged")
    @Operation(summary = "Recherche avancée (paginé)", description = "Filtres multiples avec pagination")
    public ResponseEntity<Page<BienResponseDto>> rechercheAvanceePaged(
            @Parameter(description = "Ville (optionnel)")
            @RequestParam(required = false) String ville,

            @Parameter(description = "Type de bien (optionnel)")
            @RequestParam(required = false) TypeBien typeBien,

            @Parameter(description = "Prix minimum (optionnel)")
            @RequestParam(required = false) BigDecimal prixMin,

            @Parameter(description = "Prix maximum (optionnel)")
            @RequestParam(required = false) BigDecimal prixMax,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        log.info("📥 GET /api/v1/biens/recherche/avancee/paged - Filtres: ville={}, type={}, prix={}-{}",
                ville, typeBien, prixMin, prixMax);

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BienResponseDto> biens = bienService.rechercheAvanceePaginee(ville, typeBien, prixMin, prixMax, pageable);
        return ResponseEntity.ok(biens);
    }



    // STATISTIQUES

    @GetMapping("/stats/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Statistiques par statut de validation")
    public ResponseEntity<Map<String, Long>> getStatistiques(
            @RequestParam StatutValidation statutValidation) {

        log.info("📥 GET /api/v1/biens/stats/count?statutValidation={}", statutValidation);
        long count = bienService.compterBiensParStatutValidation(statutValidation);

        Map<String, Long> response = new HashMap<>();
        response.put("statutValidation", (long) statutValidation.ordinal());
        response.put("count", count);
        return ResponseEntity.ok(response);
    }





    // ================== RECHERCHES SPATIALES POSTGIS ================== //

    @GetMapping("/recherche/proximite")
    @Operation(summary = "Rechercher des biens dans un rayon (PostGIS)", 
               description = "Cherche les biens valides et disponibles autour d'un point GPS.")
    public ResponseEntity<List<BienResponseDto>> rechercherProximite(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5") Double rayonKm) {

        log.info("📥 GET /api/v1/biens/recherche/proximite?lat={}&lon={}&rayonKm={}", latitude, longitude, rayonKm);
        List<BienResponseDto> biens = bienService.rechercherBiensProches(latitude, longitude, rayonKm);
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/recherche/zone")
    @Operation(summary = "Rechercher des biens dans une zone (Bounding Box PostGIS)", 
               description = "Cherche les biens valides visibles sur une portion de carte.")
    public ResponseEntity<List<BienResponseDto>> rechercherDansZone(
            @RequestParam Double latMin, 
            @RequestParam Double lonMin,
            @RequestParam Double latMax, 
            @RequestParam Double lonMax) {

        log.info("📥 GET /api/v1/biens/recherche/zone?latMin={}&lonMin={}&latMax={}&lonMax={}", latMin, lonMin, latMax, lonMax);
        List<BienResponseDto> biens = bienService.rechercherBiensDansZone(latMin, lonMin, latMax, lonMax);
        return ResponseEntity.ok(biens);
    }
}
