package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.ReclamationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ReclamationResponseDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.mapper.ReclamationMapper;
import univh2.fstm.gestionimmobilier.model.Contrat;
import univh2.fstm.gestionimmobilier.model.Reclamation;
import univh2.fstm.gestionimmobilier.repository.ContratRepository;
import univh2.fstm.gestionimmobilier.repository.ReclamationRepository;
import univh2.fstm.gestionimmobilier.service.MinioService;
import univh2.fstm.gestionimmobilier.service.interfaces.ReclamationService;
import univh2.fstm.gestionimmobilier.utils.ReferenceGenerator;
import univh2.fstm.gestionimmobilier.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReclamationServiceImpl implements ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final ContratRepository contratRepository;
    private final ReclamationMapper mapper;
    private final MinioService minioService;
    private final SecurityUtils securityUtils;
    private final ReferenceGenerator referenceGenerator;
    private final ReclamationMapper reclamationMapper;

    @Value("${minio.bucket.reclamations:reclamations}")
    private String bucketReclamations;

    // ============================
    // CRUD de base
    // ============================

    @Override
    public ReclamationResponseDto creerReclamation(
            ReclamationRequestDto requestDto,
            List<MultipartFile> photos) {

        log.info("📝 Création d'une réclamation - Type: {}, Priorité: {}",
                requestDto.getTypeReclamation(), requestDto.getPriorite());

        // Vérifier que le contrat existe
        Contrat contrat = contratRepository.findById(requestDto.getContratId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", requestDto.getContratId()));

        // Mapper DTO → Entité
        Reclamation reclamation = mapper.toEntity(requestDto);

        // Compléter les champs
        reclamation.setContrat(contrat);
        reclamation.setStatut(StatutReclamation.OUVERTE);

        // Générer référence unique si non fournie
        if (reclamation.getReference() == null || reclamation.getReference().isEmpty()) {
            reclamation.setReference(referenceGenerator.genererReferenceReclamation());
        }

        // Valider que la référence n'existe pas déjà
        if (reclamationRepository.existsByReference(reclamation.getReference())) {
            throw new BadRequestException("Une réclamation avec cette référence existe déjà");
        }

        // Upload photos si fournies
        if (photos != null && !photos.isEmpty()) {
            if (photos.size() > 5) {
                throw new BadRequestException("Maximum 5 photos autorisées");
            }
            List<String> photoUrls = uploadPhotos(photos);
            reclamation.setPhotos(photoUrls);
        }

        // Sauvegarder
        Reclamation savedReclamation = reclamationRepository.save(reclamation);

        log.info("✅ Réclamation créée - ID: {}, Référence: {}",
                savedReclamation.getId(), savedReclamation.getReference());

        // TODO: Envoyer notification/email au propriétaire

        return mapper.toResponseDto(savedReclamation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationResponseDto getReclamationById(Long id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        return mapper.toResponseDto(reclamation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReclamationResponseDto getReclamationByReference(String reference) {
        Reclamation reclamation = reclamationRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "référence", reference));

        return mapper.toResponseDto(reclamation);
    }

    @Override
    public ReclamationResponseDto updateReclamation(Long id, ReclamationRequestDto requestDto) {
        log.info("✏️ Mise à jour de la réclamation: {}", id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        // Vérifier que la réclamation peut être modifiée
        if (reclamation.getStatut() == StatutReclamation.RESOLUE ||
                reclamation.getStatut() == StatutReclamation.FERMEE) {
            throw new BadRequestException("Une réclamation résolue ou fermée ne peut pas être modifiée");
        }

        // Mettre à jour les champs modifiables
        if (requestDto.getTitre() != null) {
            reclamation.setTitre(requestDto.getTitre());
        }
        if (requestDto.getDescription() != null) {
            reclamation.setDescription(requestDto.getDescription());
        }
        if (requestDto.getPriorite() != null) {
            reclamation.setPriorite(requestDto.getPriorite());
        }
        if (requestDto.getTypeReclamation() != null) {
            reclamation.setTypeReclamation(requestDto.getTypeReclamation());
        }

        Reclamation updated = reclamationRepository.save(reclamation);

        log.info("✅ Réclamation mise à jour - ID: {}", id);

        return mapper.toResponseDto(updated);
    }

    @Override
    public void deleteReclamation(Long id) {
        log.info("🗑️ Suppression de la réclamation: {}", id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        // Vérifier que la réclamation peut être supprimée
        if (reclamation.getStatut() == StatutReclamation.EN_COURS ||
                reclamation.getStatut() == StatutReclamation.RESOLUE) {
            throw new BadRequestException("Une réclamation en cours ou résolue ne peut pas être supprimée");
        }

        // Supprimer les photos de MinIO
        if (reclamation.getPhotos() != null && !reclamation.getPhotos().isEmpty()) {
            for (String photoUrl : reclamation.getPhotos()) {
                try {
                    // Extraire UUID depuis l'URL et supprimer
                    minioService.deleteFile(bucketReclamations, extractUuidFromUrl(photoUrl));
                } catch (Exception e) {
                    log.warn("⚠️ Erreur suppression photo: {}", photoUrl);
                }
            }
        }

        reclamationRepository.delete(reclamation);

        log.info("✅ Réclamation supprimée - ID: {}", id);
    }

    // ============================
    // Recherches et filtres
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getAllReclamations() {
        List<Reclamation> reclamations = reclamationRepository.findAllReclamations();
        return reclamationMapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReclamationResponseDto> getAllReclamationsPaged(Pageable pageable) {
        Page<Reclamation> reclamationsPage = reclamationRepository.findAllReclamations(pageable);
        return reclamationsPage.map(reclamationMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsByContrat(Long contratId) {
        List<Reclamation> reclamations = reclamationRepository.findByContratId(contratId);
        return mapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsByLocataire(Long locataireId) {
        List<Reclamation> reclamations = reclamationRepository.findByLocataireId(locataireId);
        return mapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsByBien(Long bienId) {
        List<Reclamation> reclamations = reclamationRepository.findByBienId(bienId);
        return mapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsByProprietaire(Long proprietaireId) {
        List<Reclamation> reclamations = reclamationRepository.findByProprietaireId(proprietaireId);
        return reclamationMapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getMesReclamations() {
        Long userId = securityUtils.getCurrentUserId();

        // Vérifier si c'est un locataire
        List<Reclamation> reclamations = reclamationRepository.findByLocataireId(userId);

        return mapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> filtrerReclamations(
            StatutReclamation statut,
            PrioriteReclamation priorite,
            TypeReclamation type) {

        List<Reclamation> reclamations = reclamationRepository.findByFilters(statut, priorite, type);
        return reclamationMapper.toResponseDto(reclamations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> rechercherReclamations(String keyword) {
        List<Reclamation> reclamations = reclamationRepository.searchByKeyword(keyword);
        return reclamationMapper.toResponseDto(reclamations);
    }

    // ============================
    // Gestion du statut
    // ============================

    @Override
    public ReclamationResponseDto changerStatut(Long id, StatutReclamation nouveauStatut) {
        log.info("🔄 Changement statut réclamation {} → {}", id, nouveauStatut);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        reclamation.setStatut(nouveauStatut);

        // Si résolu, enregistrer la date
        if (nouveauStatut == StatutReclamation.RESOLUE) {
            reclamation.setDateResolution(LocalDateTime.now());
        }

        Reclamation updated = reclamationRepository.save(reclamation);

        log.info("✅ Statut changé - ID: {}, Nouveau statut: {}", id, nouveauStatut);

        return mapper.toResponseDto(updated);
    }

    @Override
    public ReclamationResponseDto prendreEnCharge(Long id, String commentaire) {
        log.info("📋 Prise en charge de la réclamation: {}", id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        if (reclamation.getStatut() != StatutReclamation.OUVERTE) {
            throw new BadRequestException("Seules les nouvelles réclamations peuvent être prises en charge");
        }

        reclamation.setStatut(StatutReclamation.EN_COURS);
        // TODO: Ajouter un champ commentaire si nécessaire

        Reclamation updated = reclamationRepository.save(reclamation);

        log.info("✅ Réclamation prise en charge - ID: {}", id);

        return mapper.toResponseDto(updated);
    }

    @Override
    public ReclamationResponseDto resoudreReclamation(Long id, String solution) {
        log.info("✅ Résolution de la réclamation: {}", id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        reclamation.setStatut(StatutReclamation.RESOLUE);
        reclamation.setDateResolution(LocalDateTime.now());
        // TODO: Ajouter un champ solution si nécessaire

        Reclamation updated = reclamationRepository.save(reclamation);

        log.info("✅ Réclamation résolue - ID: {}", id);

        return mapper.toResponseDto(updated);
    }

    @Override
    public ReclamationResponseDto fermerReclamation(Long id) {
        log.info("🔒 Fermeture de la réclamation: {}", id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        if (reclamation.getStatut() != StatutReclamation.RESOLUE) {
            throw new BadRequestException("Seules les réclamations résolues peuvent être fermées");
        }

        reclamation.setStatut(StatutReclamation.FERMEE);

        Reclamation updated = reclamationRepository.save(reclamation);

        log.info("✅ Réclamation fermée - ID: {}", id);

        return mapper.toResponseDto(updated);
    }

    @Override
    public void annulerReclamation(Long id) {
        log.info("❌ Annulation de la réclamation: {}", id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        // Vérifier que c'est le locataire propriétaire
        Long userId = securityUtils.getCurrentUserId();
        if (!reclamation.getContrat().getLocataire().getId().equals(userId)) {
            throw new BadRequestException("Vous ne pouvez pas annuler cette réclamation");
        }

        if (reclamation.getStatut() == StatutReclamation.RESOLUE ||
                reclamation.getStatut() == StatutReclamation.FERMEE) {
            throw new BadRequestException("Une réclamation résolue ou fermée ne peut pas être annulée");
        }

        reclamation.setStatut(StatutReclamation.ANNULEE);
        reclamationRepository.save(reclamation);

        log.info("✅ Réclamation annulée - ID: {}", id);
    }

    // ============================
    // Gestion des photos
    // ============================

    @Override
    public ReclamationResponseDto ajouterPhotos(Long id, List<MultipartFile> photos) {
        log.info("📷 Ajout de {} photos à la réclamation: {}", photos.size(), id);

        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", id));

        // Vérifier le nombre total de photos
        int totalPhotos = (reclamation.getPhotos() != null ? reclamation.getPhotos().size() : 0) + photos.size();
        if (totalPhotos > 5) {
            throw new BadRequestException("Maximum 5 photos par réclamation");
        }

        // Upload nouvelles photos
        List<String> nouvellesPhotos = uploadPhotos(photos);

        // Ajouter aux photos existantes
        if (reclamation.getPhotos() == null) {
            reclamation.setPhotos(new ArrayList<>());
        }
        reclamation.getPhotos().addAll(nouvellesPhotos);

        Reclamation updated = reclamationRepository.save(reclamation);

        log.info("✅ Photos ajoutées - ID: {}, Total: {}", id, updated.getPhotos().size());

        return mapper.toResponseDto(updated);
    }

    @Override
    public void supprimerPhoto(Long reclamationId, String photoUrl) {
        log.info("🗑️ Suppression d'une photo de la réclamation: {}", reclamationId);

        Reclamation reclamation = reclamationRepository.findById(reclamationId)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation", "id", reclamationId));

        if (reclamation.getPhotos() == null || !reclamation.getPhotos().contains(photoUrl)) {
            throw new BadRequestException("Cette photo n'existe pas dans la réclamation");
        }

        // Supprimer de MinIO
        try {
            String uuid = extractUuidFromUrl(photoUrl);
            minioService.deleteFile(bucketReclamations, uuid);
        } catch (Exception e) {
            log.error("❌ Erreur suppression photo de MinIO", e);
        }

        // Supprimer de la liste
        reclamation.getPhotos().remove(photoUrl);
        reclamationRepository.save(reclamation);

        log.info("✅ Photo supprimée - Réclamation ID: {}", reclamationId);
    }

    // ============================
    // Réclamations spéciales
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsUrgentes() {
        List<Reclamation> urgentes = reclamationRepository.findReclamationsUrgentesNonTraitees();
        return mapper.toResponseDto(urgentes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsEnRetard() {
        LocalDateTime dateLimit = LocalDateTime.now().minusHours(48);
        List<Reclamation> enRetard = reclamationRepository.findReclamationsEnRetard(dateLimit);
        return mapper.toResponseDto(enRetard);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReclamationResponseDto> getReclamationsRecentes() {
        LocalDateTime depuis = LocalDateTime.now().minusHours(24);
        List<Reclamation> recentes = reclamationRepository.findRecentes(depuis);
        return mapper.toResponseDto(recentes);
    }

    // ============================
    // Statistiques
    // ============================

    @Override
    @Transactional(readOnly = true)
    public long getTotalReclamations() {
        return reclamationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatut(StatutReclamation statut) {
        return reclamationRepository.countByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<StatutReclamation, Long> getStatistiquesParStatut() {
        List<Object[]> results = reclamationRepository.countByStatutGrouped();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (StatutReclamation) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TypeReclamation, Long> getStatistiquesParType() {
        List<Object[]> results = reclamationRepository.countByTypeGrouped();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (TypeReclamation) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<PrioriteReclamation, Long> getStatistiquesParPriorite() {
        List<Object[]> results = reclamationRepository.countByPrioriteGrouped();
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (PrioriteReclamation) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getDelaiResolutionMoyen() {
        // Calculer côté Java
        List<Reclamation> resolues = reclamationRepository.findByStatutOrderByCreatedOnDesc(StatutReclamation.RESOLUE);

        if (resolues.isEmpty()) {
            return 0.0;
        }

        return resolues.stream()
                .filter(r -> r.getDateResolution() != null)
                .mapToLong(r -> ChronoUnit.DAYS.between(r.getCreatedOn().toLocalDateTime(), r.getDateResolution()))
                .average()
                .orElse(0.0);
    }

    // ============================
    // Méthodes utilitaires privées
    // ============================

    private String genererReference() {
        long count = reclamationRepository.count() + 1;
        return String.format("REC-%d-%04d", LocalDateTime.now().getYear(), count);
    }

    private List<String> uploadPhotos(List<MultipartFile> photos) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile photo : photos) {
            // Valider le fichier
            if (!isImageValid(photo)) {
                throw new BadRequestException("Format d'image invalide: " + photo.getOriginalFilename());
            }

            // Upload vers MinIO
            String uuid = minioService.uploadFile(photo, bucketReclamations, "reclamations");
            String objectPath = minioService.buildObjectPath("reclamations", uuid, getExtension(photo.getOriginalFilename()));
            String url = minioService.getPresignedDownloadUrl(bucketReclamations, objectPath);

            urls.add(url);
        }

        return urls;
    }

    private boolean isImageValid(MultipartFile file) {
        // Max 10MB
        if (file.getSize() > 10 * 1024 * 1024) {
            return false;
        }

        // Types autorisés
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/webp")
        );
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    private String extractUuidFromUrl(String url) {
        try {
            // Supprimer le domaine (http://localhost:9000/)
            String path = url.substring(url.indexOf("/reclamations/"));

            // Le path est maintenant: /reclamations/reclamations/UUID-123e4567-e89b.jpg
            return path;

        } catch (Exception e) {
            log.error("❌ Erreur extraction UUID depuis URL: {}", url, e);
            throw new BadRequestException("Format d'URL invalide");
        }
    }

}