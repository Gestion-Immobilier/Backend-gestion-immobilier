package univh2.fstm.gestionimmobilier.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationRequestDto;
import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationTraitementDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeLocationResponseDto;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.mapper.DemandeLocationMapper;
import univh2.fstm.gestionimmobilier.model.*;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.DemandeLocationRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.security.JwtService;
import univh2.fstm.gestionimmobilier.service.interfaces.DemandeLocationService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DemandeLocationServiceImpl implements DemandeLocationService {

    private final DemandeLocationRepository demandeRepository;
    private final BienRepository bienRepository;
    private final PersonneRepository personneRepository;
    private final DemandeLocationMapper demandeMapper;
    private final JwtService jwtService;

    @Override
    public DemandeLocationResponseDto creerDemande(DemandeLocationRequestDto requestDto) {
        log.info("🏠 Création d'une demande de location pour le bien: {}", requestDto.getBienId());

        // Récupérer le bien
        Bien bien = bienRepository.findById(requestDto.getBienId())
                .orElseThrow(() -> new ResourceNotFoundException("Bien", "id", requestDto.getBienId()));

        // Vérifier que le bien est disponible et validé
        if (bien.getStatut() != StatutBien.DISPONIBLE) {
            throw new BadRequestException("Ce bien n'est pas disponible à la location");
        }

        if (bien.getStatutValidation() != StatutValidation.VALIDE) {
            throw new BadRequestException("Ce bien n'est pas encore validé");
        }

        // Récupérer le locataire (via SecurityContext - à adapter selon ton binôme)
        Long locataireId = getCurrentUserId();  // À implémenter
        Personne locataire = personneRepository.findById(locataireId)
                .orElseThrow(() -> new ResourceNotFoundException("Locataire", "id", locataireId));

        // Vérifier que c'est bien un locataire
        if (locataire.getType() != Type.LOCATAIRE) {
            throw new BadRequestException("Seuls les locataires peuvent faire des demandes de location");
        }

        // Vérifier qu'il n'a pas déjà une demande EN_ATTENTE pour ce bien
        if (demandeRepository.existsByLocataireIdAndBienIdAndStatut(
                locataireId, requestDto.getBienId(), StatutDemande.EN_ATTENTE)) {
            throw new BadRequestException("Vous avez déjà une demande en attente pour ce bien");
        }

        // Créer la demande
        DemandeLocation demande = demandeMapper.toEntity(requestDto);
        demande.setBien(bien);
        demande.setLocataire(locataire);
        demande.setStatut(StatutDemande.EN_ATTENTE);

        DemandeLocation demandeSauvegardee = demandeRepository.save(demande);

        log.info("✅ Demande de location créée avec succès - ID: {}", demandeSauvegardee.getId());

        // TODO: Notifier l'admin
        // notificationService.notifierDemandeLocation(demandeSauvegardee);

        return demandeMapper.toResponseDto(demandeSauvegardee);
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeLocationResponseDto getDemandeById(Long id) {
        log.debug("📥 Récupération de la demande: {}", id);

        DemandeLocation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DemandeLocation", "id", id));

        return demandeMapper.toResponseDto(demande);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeLocationResponseDto> getAllDemandes() {
        log.debug("📥 Récupération de toutes les demandes");

        List<DemandeLocation> demandes = demandeRepository.findAll();
        return demandeMapper.toResponseDto(demandes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeLocationResponseDto> getMesDemandes(Long locataireId) {
        log.debug("📥 Récupération des demandes du locataire: {}", locataireId);

        List<DemandeLocation> demandes = demandeRepository.findByLocataireId(locataireId);
        return demandeMapper.toResponseDto(demandes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeLocationResponseDto> getDemandesEnAttente() {
        log.debug("📥 Récupération des demandes en attente");

        List<DemandeLocation> demandes = demandeRepository.findByStatut(StatutDemande.EN_ATTENTE);
        return demandeMapper.toResponseDto(demandes);
    }

    @Override
    public DemandeLocationResponseDto accepterDemande(Long id) {
        log.info("✅ Acceptation de la demande: {}", id);

        DemandeLocation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DemandeLocation", "id", id));

        // Vérifier que la demande est EN_ATTENTE
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée");
        }

        // Vérifier que le bien est toujours disponible
        if (demande.getBien().getStatut() != StatutBien.DISPONIBLE) {
            throw new BadRequestException("Ce bien n'est plus disponible");
        }

        // Accepter la demande
        demande.setStatut(StatutDemande.ACCEPTEE);
        demande.setDateTraitement(LocalDateTime.now());

        DemandeLocation demandeMiseAJour = demandeRepository.save(demande);

        log.info("✅ Demande acceptée avec succès");

        // TODO: Notifier le locataire
        // notificationService.notifierDemandeAcceptee(demandeMiseAJour);

        return demandeMapper.toResponseDto(demandeMiseAJour);
    }

    @Override
    public DemandeLocationResponseDto refuserDemande(Long id, DemandeLocationTraitementDto traitementDto) {
        log.info("❌ Refus de la demande: {}", id);

        DemandeLocation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DemandeLocation", "id", id));

        // Vérifier que la demande est EN_ATTENTE
        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {
            throw new BadRequestException("Cette demande a déjà été traitée");
        }

        // Vérifier que le motif est fourni
        if (traitementDto.getMotifRefus() == null || traitementDto.getMotifRefus().isBlank()) {
            throw new BadRequestException("Le motif de refus est obligatoire");
        }

        // Refuser la demande
        demande.setStatut(StatutDemande.REFUSEE);
        demande.setMotifRefus(traitementDto.getMotifRefus());
        demande.setDateTraitement(LocalDateTime.now());

        DemandeLocation demandeMiseAJour = demandeRepository.save(demande);

        log.info("❌ Demande refusée avec succès");

        // TODO: Notifier le locataire
        // notificationService.notifierDemandeRefusee(demandeMiseAJour);

        return demandeMapper.toResponseDto(demandeMiseAJour);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterDemandesEnAttente() {
        return demandeRepository.countByStatut(StatutDemande.EN_ATTENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeLocationResponseDto> getDemandesByStatut(StatutDemande statut) {
        log.debug("📥 Récupération des demandes avec statut: {}", statut);

        List<DemandeLocation> demandes = demandeRepository.findByStatut(statut);
        return demandeMapper.toResponseDto(demandes);
    }

    // ========== Helper Method ==========

    private Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return null;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return jwtService.extractUserId(token);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'ID utilisateur", e);
        }
        return null;
    }
}