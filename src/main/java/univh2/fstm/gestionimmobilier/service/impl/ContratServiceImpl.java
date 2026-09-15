package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.ContratRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ContratResponseDto;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.mapper.ContratMapper;
import univh2.fstm.gestionimmobilier.model.*;
import univh2.fstm.gestionimmobilier.repository.*;
import univh2.fstm.gestionimmobilier.service.MinioService;
import univh2.fstm.gestionimmobilier.service.interfaces.ContratService;
import univh2.fstm.gestionimmobilier.utils.ReferenceGenerator;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ContratServiceImpl implements ContratService {

    private final ContratRepository contratRepository;
    private final BienRepository bienRepository;
    private final PersonneRepository personneRepository;
    private final DemandeLocationRepository demandeLocationRepository;
    private final ContratMapper contratMapper;
    private final ReferenceGenerator referenceGenerator;
    private final MinioService minioService;

    @Value("${minio.bucket-contrat}")
    private String bucketContrats;

    @Override
    public ContratResponseDto creerContrat(ContratRequestDto requestDto, MultipartFile documentPdf) {
        log.info("📄 Création d'un contrat pour le bien: {}", requestDto.getBienId());

        // ========== VALIDATIONS ==========

        // 1. Vérifier le bien
        Bien bien = bienRepository.findById(requestDto.getBienId())
                .orElseThrow(() -> new ResourceNotFoundException("Bien", "id", requestDto.getBienId()));

        if (bien.getStatut() != StatutBien.DISPONIBLE) {
            throw new BadRequestException("Le bien n'est pas disponible");
        }

        if (bien.getStatutValidation() != StatutValidation.VALIDE) {
            throw new BadRequestException("Le bien doit être validé");
        }

        // 2. Vérifier qu'il n'y a pas de contrat ACTIF sur ce bien
        if (contratRepository.existsByBienIdAndStatut(requestDto.getBienId(), StatutContrat.ACTIF)) {
            throw new BadRequestException("Un contrat actif existe déjà pour ce bien");
        }

        // 3. Vérifier le locataire
        Personne locataire = personneRepository.findById(requestDto.getLocataireId())
                .orElseThrow(() -> new ResourceNotFoundException("Locataire", "id", requestDto.getLocataireId()));

        if (locataire.getType() != Type.LOCATAIRE) {
            throw new BadRequestException("Cette personne n'est pas un locataire");
        }

//        if (!locataire.getVerified()) {
//            throw new BadRequestException("Le locataire doit être validé");
//        }

        // 4. Vérifier les dates
        if (requestDto.getDateFin().isBefore(requestDto.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }

        // 5. Vérifier le document PDF
        if (documentPdf == null || documentPdf.isEmpty()) {
            throw new BadRequestException("Le document PDF du contrat est obligatoire");
        }

        if (!documentPdf.getContentType().equals("application/pdf")) {
            throw new BadRequestException("Le document doit être au format PDF");
        }

        // ========== TRAÇABILITÉ DEMANDE ==========

        DemandeLocation demandeLocation = null;
        if (requestDto.getDemandeLocationId() != null) {
            demandeLocation = demandeLocationRepository.findById(requestDto.getDemandeLocationId())
                    .orElse(null);

            if (demandeLocation != null && demandeLocation.getStatut() != StatutDemande.ACCEPTEE) {
                throw new BadRequestException("La demande de location doit être acceptée");
            }
        }

        // ========== CRÉATION DU CONTRAT ==========

        Contrat contrat = contratMapper.toEntity(requestDto);
        contrat.setBien(bien);
        contrat.setLocataire(locataire);
        contrat.setDemandeLocation(demandeLocation);

        // Générer référence unique
        String reference = referenceGenerator.genererReferenceContrat();
        contrat.setReference(reference);

        // Définir statut ACTIF
        contrat.setStatut(StatutContrat.ACTIF);

        // ========== UPLOAD DU DOCUMENT VERS MINIO ==========

        try {
            String uuid = minioService.uploadFile(documentPdf, bucketContrats, "contrats");

            // Remplir les champs FileEntity
            contrat.setUuid(uuid);
            contrat.setName(documentPdf.getOriginalFilename());
            contrat.setType(documentPdf.getContentType());
            contrat.setSize(documentPdf.getSize());

            // Générer l'URL de téléchargement
            String objectPath = minioService.buildObjectPath(
                    "contrats",
                    uuid,
                    getFileExtension(documentPdf.getOriginalFilename())
            );
            String downloadUri = minioService.getPresignedDownloadUrl(bucketContrats, objectPath);
            contrat.setDownloadUri(downloadUri);

            log.info("✅ Document PDF uploadé dans MinIO - UUID: {}", uuid);

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'upload du document", e);
            throw new RuntimeException("Erreur lors de l'upload du document PDF", e);
        }

        // ========== CHANGER LE STATUT DU BIEN ==========

        bien.setStatut(StatutBien.LOUE);
        bienRepository.save(bien);

        // ========== SAUVEGARDER LE CONTRAT ==========

        Contrat contratSauvegarde = contratRepository.save(contrat);

        log.info("✅ Contrat créé avec succès - Référence: {}", reference);
        log.info("🏠 Bien {} passé en statut LOUE", bien.getReference());

        // TODO: Notifier locataire + propriétaire
        // notificationService.notifierContratCree(contratSauvegarde);

        return contratMapper.toResponseDto(contratSauvegarde);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratResponseDto getContratById(Long id) {
        log.debug("📥 Récupération du contrat: {}", id);

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));

        return contratMapper.toResponseDto(contrat);
    }

    @Override
    @Transactional(readOnly = true)
    public ContratResponseDto getContratByReference(String reference) {
        log.debug("📥 Récupération du contrat avec référence: {}", reference);

        Contrat contrat = contratRepository.findByReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "reference", reference));

        return contratMapper.toResponseDto(contrat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratResponseDto> getAllContrats() {
        log.debug("📥 Récupération de tous les contrats");

        List<Contrat> contrats = contratRepository.findAll();
        return contratMapper.toResponseDto(contrats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ContratResponseDto> getAllContratsPaged(Pageable pageable) {
        log.debug("📥 Récupération de tous les contrats (paginé)");
        Page<Contrat> contrats = contratRepository.findAll(pageable);
        return contrats.map(contratMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratResponseDto> getContratsLocataire(Long locataireId) {
        log.debug("📥 Récupération des contrats du locataire: {}", locataireId);

        List<Contrat> contrats = contratRepository.findByLocataireId(locataireId);
        return contratMapper.toResponseDto(contrats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ContratResponseDto> getContratsLocatairePaged(Long locataireId, Pageable pageable) {
        log.debug("📥 Récupération des contrats du locataire: {} (paginé)", locataireId);
        Page<Contrat> contrats = contratRepository.findByLocataireId(locataireId, pageable);
        return contrats.map(contratMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratResponseDto> getContratsBien(Long bienId) {
        log.debug("📥 Récupération des contrats du bien: {}", bienId);

        List<Contrat> contrats = contratRepository.findByBienId(bienId);
        return contratMapper.toResponseDto(contrats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ContratResponseDto> getContratsBienPaged(Long bienId, Pageable pageable) {
        log.debug("📥 Récupération des contrats du bien: {} (paginé)", bienId);
        Page<Contrat> contrats = contratRepository.findByBienId(bienId, pageable);
        return contrats.map(contratMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratResponseDto> getContratsProprietaire(Long proprietaireId) {
        log.debug("📥 Récupération des contrats du propriétaire: {}", proprietaireId);

        List<Contrat> contrats = contratRepository.findByProprietaireId(proprietaireId);
        return contratMapper.toResponseDto(contrats);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ContratResponseDto> getContratsProprietairePaged(Long proprietaireId, Pageable pageable) {
        log.debug("📥 Récupération des contrats du propriétaire: {} (paginé)", proprietaireId);
        Page<Contrat> contrats = contratRepository.findByProprietaireId(proprietaireId, pageable);
        return contrats.map(contratMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContratResponseDto> getContratsByStatut(StatutContrat statut) {
        log.debug("📥 Récupération des contrats avec statut: {}", statut);

        List<Contrat> contrats = contratRepository.findByStatut(statut);
        return contratMapper.toResponseDto(contrats);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContratResponseDto> getContratsByStatutPaged(StatutContrat statut, Pageable pageable) {
        log.debug("📥 Récupération des contrats avec statut: {} (paginé)", statut);
        Page<Contrat> contrats = contratRepository.findByStatut(statut, pageable);
        return contrats.map(contratMapper::toResponseDto);
    }

    @Override
    public ContratResponseDto updateContrat(Long id, ContratRequestDto requestDto, MultipartFile documentPdf) {
        log.info("✏️ Mise à jour du contrat: {}", id);

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));

        // Vérifier que le contrat est ACTIF
        if (contrat.getStatut() != StatutContrat.ACTIF) {
            throw new BadRequestException("Seuls les contrats actifs peuvent être modifiés");
        }

        // Mettre à jour les champs modifiables
        if (requestDto.getDateFin() != null) {
            if (requestDto.getDateFin().isBefore(contrat.getDateDebut())) {
                throw new BadRequestException("La date de fin doit être après la date de début");
            }
            contrat.setDateFin(requestDto.getDateFin());
        }

        if (requestDto.getLoyerMensuel() != null) {
            contrat.setLoyerMensuel(requestDto.getLoyerMensuel());
        }

        if (requestDto.getCharges() != null) {
            contrat.setCharges(requestDto.getCharges());
        }

        if (requestDto.getJourPaiement() != null) {
            contrat.setJourPaiement(requestDto.getJourPaiement());
        }

        if (requestDto.getClausesParticulieres() != null) {
            contrat.setClausesParticulieres(requestDto.getClausesParticulieres());
        }

        // Mettre à jour le document si fourni
        if (documentPdf != null && !documentPdf.isEmpty()) {
            if (!documentPdf.getContentType().equals("application/pdf")) {
                throw new BadRequestException("Le document doit être au format PDF");
            }

            // Supprimer l'ancien document
            if (contrat.getUuid() != null) {
                String oldObjectPath = minioService.buildObjectPath(
                        "contrats",
                        contrat.getUuid(),
                        getFileExtension(contrat.getName())
                );
                minioService.deleteFile(bucketContrats, oldObjectPath);
            }

            // Upload le nouveau document
            String uuid = minioService.uploadFile(documentPdf, bucketContrats, "contrats");
            contrat.setUuid(uuid);
            contrat.setName(documentPdf.getOriginalFilename());
            contrat.setType(documentPdf.getContentType());
            contrat.setSize(documentPdf.getSize());

            String objectPath = minioService.buildObjectPath(
                    "contrats",
                    uuid,
                    getFileExtension(documentPdf.getOriginalFilename())
            );
            String downloadUri = minioService.getPresignedDownloadUrl(bucketContrats, objectPath);
            contrat.setDownloadUri(downloadUri);

            log.info("✅ Nouveau document PDF uploadé - UUID: {}", uuid);
        }

        Contrat contratMisAJour = contratRepository.save(contrat);

        log.info("✅ Contrat mis à jour avec succès");
        return contratMapper.toResponseDto(contratMisAJour);
    }

    @Override
    public ContratResponseDto resilierContrat(Long id) {
        log.info("🔴 Résiliation du contrat: {}", id);

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));

        // Vérifier que le contrat est ACTIF
        if (contrat.getStatut() != StatutContrat.ACTIF) {
            throw new BadRequestException("Seuls les contrats actifs peuvent être résiliés");
        }

        // Résilier le contrat
        contrat.setStatut(StatutContrat.RESILIE);

        // Remettre le bien en DISPONIBLE
        Bien bien = contrat.getBien();
        bien.setStatut(StatutBien.DISPONIBLE);
        bienRepository.save(bien);

        Contrat contratResilie = contratRepository.save(contrat);

        log.info("✅ Contrat résilié avec succès");
        log.info("🏠 Bien {} remis en statut DISPONIBLE", bien.getReference());

        // TODO: Notifier locataire + propriétaire
        // notificationService.notifierContratResilie(contratResilie);

        return contratMapper.toResponseDto(contratResilie);
    }

    @Override
    public void deleteContrat(Long id) {
        log.info("🗑️ Suppression du contrat: {}", id);

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));

        // Supprimer le document de MinIO
        if (contrat.getUuid() != null) {
            String objectPath = minioService.buildObjectPath(
                    "contrats",
                    contrat.getUuid(),
                    getFileExtension(contrat.getName())
            );
            minioService.deleteFile(bucketContrats, objectPath);
            log.info("🗑️ Document supprimé de MinIO");
        }

        // Si le contrat était ACTIF, remettre le bien en DISPONIBLE
        if (contrat.getStatut() == StatutContrat.ACTIF) {
            Bien bien = contrat.getBien();
            bien.setStatut(StatutBien.DISPONIBLE);
            bienRepository.save(bien);
            log.info("🏠 Bien {} remis en DISPONIBLE", bien.getReference());
        }

        contratRepository.delete(contrat);
        log.info("✅ Contrat supprimé avec succès");
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream downloadDocumentContrat(Long id) {
        log.info("📥 Téléchargement du document du contrat: {}", id);

        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));

        if (contrat.getUuid() == null) {
            throw new ResourceNotFoundException("Document non trouvé pour ce contrat");
        }

        String objectPath = minioService.buildObjectPath(
                "contrats",
                contrat.getUuid(),
                getFileExtension(contrat.getName())
        );

        return minioService.downloadFile(bucketContrats, objectPath);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDocumentContentType(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", id));

        if (contrat.getUuid() == null) {
            return "application/pdf";
        }

        String objectPath = minioService.buildObjectPath(
                "contrats",
                contrat.getUuid(),
                getFileExtension(contrat.getName())
        );

        return minioService.getContentType(bucketContrats, objectPath);
    }

    @Override
    public void verifierContratsExpires() {
        log.info("🕐 Vérification des contrats expirés...");

        List<Contrat> contratsExpires = contratRepository.findContratsExpires(LocalDate.now());

        for (Contrat contrat : contratsExpires) {
            contrat.setStatut(StatutContrat.EXPIRE);

            // Remettre le bien en DISPONIBLE
            Bien bien = contrat.getBien();
            bien.setStatut(StatutBien.DISPONIBLE);
            bienRepository.save(bien);

            contratRepository.save(contrat);

            log.info("⏰ Contrat {} expiré - Bien {} remis en DISPONIBLE",
                    contrat.getReference(), bien.getReference());

            // TODO: Notifier locataire + propriétaire
            // notificationService.notifierContratExpire(contrat);
        }

        log.info("✅ {} contrat(s) expiré(s) traité(s)", contratsExpires.size());
    }

    // ========== Helper Methods ==========

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".pdf";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}