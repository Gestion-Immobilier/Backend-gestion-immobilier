package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univh2.fstm.gestionimmobilier.dto.request.DemandeResiliationRequestDto;
import univh2.fstm.gestionimmobilier.dto.request.TraiterDemandeResiliationDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeResiliationResponseDto;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.mapper.DemandeResiliationMapper;
import univh2.fstm.gestionimmobilier.model.*;
import univh2.fstm.gestionimmobilier.repository.ContratRepository;
import univh2.fstm.gestionimmobilier.repository.DemandeResiliationRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.service.interfaces.DemandeResiliationService;
import univh2.fstm.gestionimmobilier.utils.SecurityUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DemandeResiliationServiceImpl implements DemandeResiliationService {

    private final DemandeResiliationRepository demandeRepository;
    private final ContratRepository contratRepository;
    private final PersonneRepository locataireRepository;
    private final DemandeResiliationMapper mapper;
    private final SecurityUtils securityUtils;


    @Override
    public DemandeResiliationResponseDto creerDemande(DemandeResiliationRequestDto requestDto) {
        Long userId = securityUtils.getCurrentUserId();
        Personne locataire = locataireRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Locataire non trouvé"));

        Contrat contrat = contratRepository.findById(requestDto.getContratId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrat", "id", requestDto.getContratId()));

        // Vérifications
        if (!contrat.getLocataire().getId().equals(locataire.getId())) {
            throw new BadRequestException("Ce contrat ne vous appartient pas");

        }

        if (contrat.getStatut() != StatutContrat.ACTIF) {
            throw new BadRequestException("Seuls les contrats actifs peuvent faire l'objet d'une demande de résiliation");
        }

        // Vérifier s'il n'y a pas déjà une demande en cours
        if (demandeRepository.existsDemandeEnCoursByLocataireAndContrat(locataire.getId(), contrat.getId())) {
            throw new BadRequestException("Une demande de résiliation est déjà en cours pour ce contrat");
        }

        if (requestDto.getDateSouhaiteeResiliation().isBefore(LocalDate.now())) {
            throw new BadRequestException("La date de résiliation souhaitée ne peut pas être dans le passé");
        }

        DemandeResiliation demande = DemandeResiliation.builder()
                .contrat(contrat)
                .locataire(locataire)
                .typeResiliation(requestDto.getTypeResiliation())
                .dateDemandeResiliation(LocalDate.now())
                .dateSouhaiteeResiliation(requestDto.getDateSouhaiteeResiliation())
                .statut(StatutDemandeResiliation.EN_ATTENTE)
                .motif(requestDto.getMotif())
                .commentaireLocataire(requestDto.getCommentaireLocataire())
                .build();


        DemandeResiliation savedDemande = demandeRepository.save(demande);

        log.info("✅ Demande de résiliation créée avec succès - ID: {}", savedDemande.getId());
        return mapper.toResponseDto(savedDemande);

    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResiliationResponseDto> getMesDemandesResiliation() {
        Long userId = securityUtils.getCurrentUserId();
        Personne locataire = locataireRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Locataire non trouvé"));

        List<DemandeResiliation> demandes = demandeRepository
                .findByLocataireId(locataire.getId());

        return demandes.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeResiliationResponseDto getDemandeById(Long id) {
        DemandeResiliation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de résiliation", "id", id));
        return mapper.toResponseDto(demande);
    }

    @Override
    public void annulerDemande(Long id) {

        DemandeResiliation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de résiliation", "id", id));

        Long userId = securityUtils.getCurrentUserId();
        Personne locataire = locataireRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Locataire non trouvé"));
        if (!demande.getLocataire().getId().equals(locataire.getId())) {
            throw new BadRequestException("Vous ne pouvez pas annuler cette demande");
        }

        if (demande.getStatut() == StatutDemandeResiliation.ACCEPTEE ||
                demande.getStatut() == StatutDemandeResiliation.REFUSEE) {
            throw new BadRequestException("Cette demande a déjà été traitée et ne peut plus être annulée");
        }

        demande.setStatut(StatutDemandeResiliation.ANNULEE);
        demandeRepository.save(demande);

        log.info("✅ Demande de résiliation annulée - ID: {}", id);

    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResiliationResponseDto> getAllDemandes() {
        List<DemandeResiliation> demandes = demandeRepository.findAllDemandes();
        return mapper.toResponseDtoList(demandes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DemandeResiliationResponseDto> getDemandesByStatut(StatutDemandeResiliation statut) {
        List<DemandeResiliation> demandes = demandeRepository
                .findByStatut(statut);

        return mapper.toResponseDtoList(demandes);
    }

    @Override
    public DemandeResiliationResponseDto traiterDemande(Long id, TraiterDemandeResiliationDto traiterDto) {
        log.info("📋 Traitement de la demande de résiliation: {}", id);

        DemandeResiliation demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande de résiliation", "id", id));

        // Vérifications
        if (demande.getStatut() != StatutDemandeResiliation.EN_ATTENTE &&
                demande.getStatut() != StatutDemandeResiliation.EN_COURS_EXAMEN) {
            throw new BadRequestException("Cette demande a déjà été traitée");

        }

        if (traiterDto.getStatut() != StatutDemandeResiliation.ACCEPTEE &&
                traiterDto.getStatut() != StatutDemandeResiliation.REFUSEE) {
            throw new BadRequestException("Le statut doit être ACCEPTEE ou REFUSEE");
        }



// Traiter la demande
        demande.setStatut(traiterDto.getStatut());
        demande.setDateTraitement(LocalDate.now());
        demande.setCommentaireAdmin(traiterDto.getCommentaireAdmin());

        if (traiterDto.getStatut() == StatutDemandeResiliation.ACCEPTEE) {
            if (traiterDto.getDateResiliationEffective() == null) {
                throw new BadRequestException("La date de résiliation effective est obligatoire");
            }
            demande.setDateResiliationEffective(traiterDto.getDateResiliationEffective());

            // Mettre à jour le contrat
            Contrat contrat = demande.getContrat();
            contrat.setStatut(StatutContrat.RESILIE);
            contrat.setDateFin(traiterDto.getDateResiliationEffective());
            contratRepository.save(contrat);

            log.info("✅ Contrat résilié - Numéro: {}", contrat.getReference());
        }

        DemandeResiliation savedDemande = demandeRepository.save(demande);

        log.info("✅ Demande de résiliation traitée - Statut: {}", traiterDto.getStatut());
        return mapper.toResponseDto(savedDemande);

    }

    @Override
    @Transactional(readOnly = true)
    public long countDemandesEnAttente() {

        return demandeRepository.countByStatut(StatutDemandeResiliation.EN_ATTENTE);
    }
}
