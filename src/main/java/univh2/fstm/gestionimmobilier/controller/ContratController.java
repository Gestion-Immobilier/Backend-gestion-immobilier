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
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.ContratRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ContratResponseDto;
import univh2.fstm.gestionimmobilier.model.StatutContrat;
import univh2.fstm.gestionimmobilier.service.interfaces.ContratService;
import univh2.fstm.gestionimmobilier.utils.SecurityUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/contrats")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contrats", description = "API de gestion des contrats de location")
public class ContratController {

    private final ContratService contratService;
    private final SecurityUtils securityUtils;


    // ========================================
    // CRÉATION ET CONSULTATION
    // ========================================

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer un contrat", description = "ADMIN uniquement - Avec document PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrat créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou document manquant"),
            @ApiResponse(responseCode = "404", description = "Bien ou locataire introuvable")
    })
    public ResponseEntity<ContratResponseDto> creerContrat(
            @Parameter(description = "Données du contrat au format JSON", required = true)
            @RequestParam("contrat") String contratJson,
            @Parameter(description = "Document PDF", required = true)
            @RequestParam("document") MultipartFile documentPdf) {

        log.info("📥 POST /api/v1/contrats - Création d'un contrat");

        try {
            // Désérialiser le JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            ContratRequestDto requestDto = objectMapper.readValue(contratJson, ContratRequestDto.class);

            log.info("✅ JSON parsé avec succès pour le bien: {}", requestDto.getBienId());

            // Valider le DTO
            validateContratRequest(requestDto);

            ContratResponseDto response = contratService.creerContrat(requestDto, documentPdf);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            log.error("❌ Erreur de parsing JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ConstraintViolationException e) {
            log.error("❌ Validation échouée: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private void validateContratRequest(ContratRequestDto dto) {
        Set<ConstraintViolation<ContratRequestDto>> violations =
                Validation.buildDefaultValidatorFactory().getValidator().validate(dto);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @contratSecurityService.isPartiePrenanteContrat(#id)")
    @Operation(summary = "Récupérer un contrat par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    public ResponseEntity<ContratResponseDto> getContratById(@PathVariable Long id) {
        log.info("📥 GET /api/v1/contrats/{}", id);
        ContratResponseDto response = contratService.getContratById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/reference/{reference}")
    @PreAuthorize("hasRole('ADMIN') or @contratSecurityService.isPartiePrenanteContratByReference(#reference)")
    @Operation(summary = "Récupérer un contrat par référence")
    public ResponseEntity<ContratResponseDto> getContratByReference(@PathVariable String reference) {
        log.info("📥 GET /api/v1/contrats/reference/{}", reference);
        ContratResponseDto response = contratService.getContratByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer tous les contrats", description = "ADMIN uniquement")
    public ResponseEntity<List<ContratResponseDto>> getAllContrats() {
        log.info("📥 GET /api/v1/contrats");
        List<ContratResponseDto> contrats = contratService.getAllContrats();
        return ResponseEntity.ok(contrats);
    }
    
    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer tous les contrats (paginé)", description = "ADMIN uniquement, avec pagination")
    public ResponseEntity<Page<ContratResponseDto>> getAllContratsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/contrats/paged");
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratResponseDto> contrats = contratService.getAllContratsPaged(pageable);
        return ResponseEntity.ok(contrats);
    }

    // ========================================
    // FILTRES PAR UTILISATEUR
    // ========================================

    @GetMapping("/locataire/{locataireId}")
    @PreAuthorize("hasRole('ADMIN') or @contratSecurityService.isLocataire(#locataireId)")
    @Operation(summary = "Contrats d'un locataire")
    public ResponseEntity<List<ContratResponseDto>> getContratsLocataire(@PathVariable Long locataireId) {
        log.info("📥 GET /api/v1/contrats/locataire/{}", locataireId);
        List<ContratResponseDto> contrats = contratService.getContratsLocataire(locataireId);
        return ResponseEntity.ok(contrats);
    }
    
    @GetMapping("/locataire/{locataireId}/paged")
    @PreAuthorize("hasRole('ADMIN') or @contratSecurityService.isLocataire(#locataireId)")
    @Operation(summary = "Contrats d'un locataire (paginé)")
    public ResponseEntity<Page<ContratResponseDto>> getContratsLocatairePaged(
            @PathVariable Long locataireId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/contrats/locataire/{}/paged", locataireId);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratResponseDto> contrats = contratService.getContratsLocatairePaged(locataireId, pageable);
        return ResponseEntity.ok(contrats);
    }

    @GetMapping("/mes-contrats")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Mes contrats", description = "Contrats du locataire connecté")
    public ResponseEntity<List<ContratResponseDto>> getMesContrats() {
        log.info("📥 GET /api/v1/contrats/mes-contrats");

        Long locataireId = securityUtils.getCurrentUserId(); // ✅
        List<ContratResponseDto> contrats = contratService.getContratsLocataire(locataireId);
        return ResponseEntity.ok(contrats);
    }
    
    @GetMapping("/mes-contrats/paged")
    @PreAuthorize("hasRole('LOCATAIRE')")
    @Operation(summary = "Mes contrats (paginé)", description = "Contrats du locataire connecté avec pagination")
    public ResponseEntity<Page<ContratResponseDto>> getMesContratsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/contrats/mes-contrats/paged");

        Long locataireId = securityUtils.getCurrentUserId();
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratResponseDto> contrats = contratService.getContratsLocatairePaged(locataireId, pageable);
        return ResponseEntity.ok(contrats);
    }

    @GetMapping("/bien/{bienId}")
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaireDuBien(#bienId)")
    @Operation(summary = "Contrats d'un bien", description = "Historique de location")
    public ResponseEntity<List<ContratResponseDto>> getContratsBien(@PathVariable Long bienId) {
        log.info("📥 GET /api/v1/contrats/bien/{}", bienId);
        List<ContratResponseDto> contrats = contratService.getContratsBien(bienId);
        return ResponseEntity.ok(contrats);
    }
    
    @GetMapping("/bien/{bienId}/paged")
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaireDuBien(#bienId)")
    @Operation(summary = "Contrats d'un bien (paginé)", description = "Historique de location avec pagination")
    public ResponseEntity<Page<ContratResponseDto>> getContratsBienPaged(
            @PathVariable Long bienId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/contrats/bien/{}/paged", bienId);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratResponseDto> contrats = contratService.getContratsBienPaged(bienId, pageable);
        return ResponseEntity.ok(contrats);
    }

    @GetMapping("/proprietaire/{proprietaireId}")
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaire(#proprietaireId)")
    @Operation(summary = "Contrats d'un propriétaire")
    public ResponseEntity<List<ContratResponseDto>> getContratsProprietaire(@PathVariable Long proprietaireId) {
        log.info("📥 GET /api/v1/contrats/proprietaire/{}", proprietaireId);
        List<ContratResponseDto> contrats = contratService.getContratsProprietaire(proprietaireId);
        return ResponseEntity.ok(contrats);
    }

    @GetMapping("/proprietaire/{proprietaireId}/paged")
    @PreAuthorize("hasRole('ADMIN') or @bienSecurityService.isProprietaire(#proprietaireId)")
    @Operation(summary = "Contrats d'un propriétaire (paginé)")
    public ResponseEntity<Page<ContratResponseDto>> getContratsProprietairePaged(
            @PathVariable Long proprietaireId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/contrats/proprietaire/{}/paged", proprietaireId);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratResponseDto> contrats = contratService.getContratsProprietairePaged(proprietaireId, pageable);
        return ResponseEntity.ok(contrats);
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Contrats par statut", description = "ADMIN uniquement")
    public ResponseEntity<List<ContratResponseDto>> getContratsByStatut(@PathVariable StatutContrat statut) {
        log.info("📥 GET /api/v1/contrats/statut/{}", statut);
        List<ContratResponseDto> contrats = contratService.getContratsByStatut(statut);
        return ResponseEntity.ok(contrats);
    }
    
    @GetMapping("/statut/{statut}/paged")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Contrats par statut (paginé)", description = "ADMIN uniquement")
    public ResponseEntity<Page<ContratResponseDto>> getContratsByStatutPaged(
            @PathVariable StatutContrat statut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        log.info("📥 GET /api/v1/contrats/statut/{}/paged", statut);
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ContratResponseDto> contrats = contratService.getContratsByStatutPaged(statut, pageable);
        return ResponseEntity.ok(contrats);
    }

    // ========================================
    // MODIFICATION ET ACTIONS
    // ========================================

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Mettre à jour un contrat", description = "ADMIN uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat mis à jour"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    public ResponseEntity<ContratResponseDto> updateContrat(
            @PathVariable Long id,
            @Valid @RequestParam("contrat") String contratJson,
            @RequestParam(value = "document", required = false) MultipartFile documentPdf) throws JsonProcessingException {


        log.info("📥 PUT /api/v1/contrats/{}", id);
        try {
            // Désérialiser le JSON
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            ContratRequestDto requestDto = objectMapper.readValue(contratJson, ContratRequestDto.class);

            log.info("✅ JSON parsé avec succès pour le bien: {}", requestDto.getBienId());

            // Valider le DTO
            validateContratRequest(requestDto);

            ContratResponseDto response = contratService.updateContrat(id,requestDto, documentPdf);
        return ResponseEntity.ok(response);
        } catch (JsonProcessingException e) {
            log.error("❌ Erreur de parsing JSON: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (ConstraintViolationException e) {
            log.error("❌ Validation échouée: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/resilier")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Résilier un contrat", description = "ADMIN uniquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat résilié"),
            @ApiResponse(responseCode = "400", description = "Contrat non actif"),
            @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    public ResponseEntity<ContratResponseDto> resilierContrat(@PathVariable Long id) {
        log.info("📥 PATCH /api/v1/contrats/{}/resilier", id);
        ContratResponseDto response = contratService.resilierContrat(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un contrat", description = "ADMIN uniquement")
    @ApiResponse(responseCode = "204", description = "Contrat supprimé")
    public ResponseEntity<Void> deleteContrat(@PathVariable Long id) {
        log.info("📥 DELETE /api/v1/contrats/{}", id);
        contratService.deleteContrat(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================
    // TÉLÉCHARGEMENT DOCUMENT
    // ========================================

    @GetMapping("/{id}/document")
    @PreAuthorize("hasRole('ADMIN') or @contratSecurityService.isPartiePrenanteContrat(#id)")
    @Operation(summary = "Télécharger le document PDF", description = "Télécharge le contrat signé")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document téléchargé"),
            @ApiResponse(responseCode = "404", description = "Contrat ou document introuvable")
    })
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long id) {
        log.info("📥 GET /api/v1/contrats/{}/document", id);

        InputStream documentStream = contratService.downloadDocumentContrat(id);
        String contentType = contratService.getDocumentContentType(id);

        // Récupérer le nom du fichier depuis le contrat
        ContratResponseDto contrat = contratService.getContratById(id);
        String filename = contrat.getName() != null
                ? contrat.getName()
                : "contrat-" + contrat.getReference() + ".pdf";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add(HttpHeaders.CONTENT_TYPE, contentType);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(new InputStreamResource(documentStream));
    }

    // ========================================
    // TÂCHES AUTOMATIQUES
    // ========================================

    @PostMapping("/verifier-expiration")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Vérifier les contrats expirés", description = "Job manuel - ADMIN uniquement")
    @ApiResponse(responseCode = "200", description = "Vérification effectuée")
    public ResponseEntity<String> verifierContratsExpires() {
        log.info("📥 POST /api/v1/contrats/verifier-expiration");
        contratService.verifierContratsExpires();
        return ResponseEntity.ok("Vérification des contrats expirés effectuée avec succès");
    }
}