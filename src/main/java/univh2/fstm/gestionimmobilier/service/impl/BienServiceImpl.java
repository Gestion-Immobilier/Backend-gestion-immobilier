package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univh2.fstm.gestionimmobilier.dto.request.BienRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.BienResponseDto;
import univh2.fstm.gestionimmobilier.dto.request.BienValidationDto;
import univh2.fstm.gestionimmobilier.exception.BadRequestException;
import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
import univh2.fstm.gestionimmobilier.mapper.BienMapper;
import univh2.fstm.gestionimmobilier.model.*;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.service.interfaces.BienService;
import univh2.fstm.gestionimmobilier.utils.ReferenceGenerator;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BienServiceImpl implements BienService {
    private final BienRepository bienRepository;
    private final PersonneRepository personneRepository;
    private final BienMapper bienMapper;
    private final ReferenceGenerator referenceGenerator;



    @Override
    public BienResponseDto creerBien(BienRequestDto requestDto) {
        log.info("Création d'un nouveau bien de type: {}", requestDto.getTypeBien());
        Bien bien = bienMapper.toEntity(requestDto);
        // ========== NOUVEAU : Associer le propriétaire ==========
        Personne proprietaire = personneRepository.findById(requestDto.getProprietaireId())
                .orElseThrow(() -> new ResourceNotFoundException("Propriétaire", "id", requestDto.getProprietaireId()));

        // Vérifier que c'est bien un propriétaire
        if (proprietaire.getType() != Type.PROPRIETAIRE) {
            throw new BadRequestException("Cette personne n'est pas un propriétaire");
        }

        // Vérifier que le propriétaire est validé
        if (!proprietaire.getVerified()) {
            throw new BadRequestException("Le propriétaire doit être validé par un admin");
        }

        bien.setProprietaire(proprietaire);


        //generer ref unique
        String reference = referenceGenerator.genererReferenceBien(bien.getTypeBien());
        bien.setReference(reference);
        //definir le statut
        bien.setStatutValidation(StatutValidation.EN_ATTENTE);

        // Définir statut bien par défaut si non fourni
        if (bien.getStatut() == null) {
            bien.setStatut(StatutBien.DISPONIBLE);
        }

        // Valeurs par défaut pour booléens
        if (bien.getMeuble() == null) bien.setMeuble(false);
        if (bien.getBalcon() == null) bien.setBalcon(false);
        if (bien.getParking() == null) bien.setParking(false);
        if (bien.getAscenseur() == null) bien.setAscenseur(false);

        // et enfin on save le bien
        Bien bienSauvegarde = bienRepository.save(bien);

        log.info("Bien créé avec succès - Référence: {}", reference);
        return bienMapper.toResponseDto(bienSauvegarde);
    }



    @Override
    @Transactional(readOnly = true)
    public BienResponseDto getBienById(Long id) {
        log.debug("Recuperation du bien avec ID : {}",id);
        Bien bien = bienRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Bien","id",id));


        return bienMapper.toResponseDto(bien);
    }

    @Override
    @Transactional(readOnly = true)
    public BienResponseDto getBienByReference(String reference) {
        log.debug("Recuperation du bien avec reference : {}",reference);
        Bien bien = bienRepository.findBienByReference(reference).orElseThrow(()-> new ResourceNotFoundException("Bien","reference",reference));

        return bienMapper.toResponseDto(bien);
    }


    @Override
    public List<BienResponseDto> getAllBiens() {
        log.debug("Recuperation de tous les biens");
        List<Bien> biens = bienRepository.findAll();

        return bienMapper.toResponseDto(biens);
    }

    @Override
    public BienResponseDto updateBien(Long id, BienRequestDto requestDto) {
        log.info("Mettre a jour le bien d'id: {}",id);
        //je recupere le bien existant
        Bien bienExistant = bienRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Bien","id",id));

        // Vérifier si référence est modifiée et unique
        if (requestDto.getReference() != null &&
                !requestDto.getReference().equals(bienExistant.getReference())) {
            if (bienRepository.existsByReference(requestDto.getReference())) {
                throw new BadRequestException("Cette référence existe déjà");
            }
        }

        // Mettre à jour les champs , fuck but we still have to do this shit even in AI era
        if (requestDto.getTypeBien() != null)
            bienExistant.setTypeBien(requestDto.getTypeBien());
        if (requestDto.getAdresse() != null)
            bienExistant.setAdresse(requestDto.getAdresse());
        if (requestDto.getVille() != null)
            bienExistant.setVille(requestDto.getVille());
        if (requestDto.getCodePostal() != null)
            bienExistant.setCodePostal(requestDto.getCodePostal());
        if (requestDto.getSurface() != null)
            bienExistant.setSurface(requestDto.getSurface());
        if (requestDto.getNombrePieces() != null)
            bienExistant.setNombrePieces(requestDto.getNombrePieces());
        if (requestDto.getNombreChambres() != null)
            bienExistant.setNombreChambres(requestDto.getNombreChambres());
        if (requestDto.getNombreSallesBain() != null)
            bienExistant.setNombreSallesBain(requestDto.getNombreSallesBain());
        if (requestDto.getDescription() != null)
            bienExistant.setDescription(requestDto.getDescription());
        if (requestDto.getLoyerMensuel() != null)
            bienExistant.setLoyerMensuel(requestDto.getLoyerMensuel());
        if (requestDto.getCharges() != null)
            bienExistant.setCharges(requestDto.getCharges());
        if (requestDto.getCaution() != null)
            bienExistant.setCaution(requestDto.getCaution());
        if (requestDto.getStatut() != null)
            bienExistant.setStatut(requestDto.getStatut());
        if (requestDto.getDateAcquisition() != null)
            bienExistant.setDateAcquisition(requestDto.getDateAcquisition());
        if (requestDto.getPhotos() != null)
            bienExistant.setPhotos(requestDto.getPhotos());
        if (requestDto.getMeuble() != null)
            bienExistant.setMeuble(requestDto.getMeuble());
        if (requestDto.getBalcon() != null)
            bienExistant.setBalcon(requestDto.getBalcon());
        if (requestDto.getParking() != null)
            bienExistant.setParking(requestDto.getParking());
        if (requestDto.getAscenseur() != null)
            bienExistant.setAscenseur(requestDto.getAscenseur());

        // enfin on save
        Bien bienMisAjour = bienRepository.save(bienExistant);

        log.info("Bien mis a jour avec succes - ID: {}",id);

        return bienMapper.toResponseDto(bienMisAjour);
    }


    @Override
    public BienResponseDto changerStatutBien(Long id, StatutBien nouveauStatut) {
        log.info("Changement du statut du bien {} vers {}",id,nouveauStatut);

        Bien bien=bienRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Bien","id",id));
        bien.setStatut(nouveauStatut);
        Bien bienMisAJour = bienRepository.save(bien);

        log.info("Bien mis a jour avec succes");

        return bienMapper.toResponseDto(bienMisAJour);
    }

    @Override
    public void deleteBien(Long id) {
        log.info("Suppression du bien d'id : {}",id);

        if(!bienRepository.existsById(id)){
            throw new ResourceNotFoundException("Bien","id",id);
        }
        bienRepository.deleteById(id);
        log.info("Bien supprime avec succes");
    }

    // ici la validation de l'agent

    @Override
    @Transactional(readOnly = true)
    public List<BienResponseDto> getBiensEnAttente() {
        log.debug("Recuperer les biens en attente de validation ");
        List<Bien> biens = bienRepository.findByStatutValidation(StatutValidation.EN_ATTENTE);

        return bienMapper.toResponseDto(biens);
    }

    @Override
    public BienResponseDto validerBien(Long id, BienValidationDto validationDto) {
        log.info("Valider le bien {} avec le statut: {}",id,validationDto.getStatutValidation());
        Bien bien = bienRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Bien","id",id));

        // Validation métier : si rejeté, motif obligatoire
        if(validationDto.getStatutValidation() == StatutValidation.REJETE && (validationDto.getMotifRejet() == null || validationDto.getMotifRejet().isBlank())){
            throw new BadRequestException("le motif de rejet est obligatoire");
        }

        // on met a jour le status
        bien.setStatutValidation(validationDto.getStatutValidation());
        bien.setMotifRejet(validationDto.getMotifRejet());

        Bien bienValide = bienRepository.save(bien);
        log.info("Bien {} valide avec succes",id);

        return bienMapper.toResponseDto(bienValide);
    }

    @Override
    @Transactional(readOnly = true)
    public long compterBiensEnAttente() {
        return bienRepository.countByStatutValidation(StatutValidation.EN_ATTENTE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponseDto> getBiensByProprietaire(Long proprietaireId) {
        log.debug("Récupération des biens du propriétaire: {}", proprietaireId);

        List<Bien> biens = bienRepository.findByProprietaireId(proprietaireId);
        return bienMapper.toResponseDto(biens);
    }

    // la recherche pour les clients public
    @Override
    @Transactional(readOnly = true)
    public List<BienResponseDto> getBiensPublics() {
        log.debug("Recuperer les biens valides et dispo");
        List<Bien> biens = bienRepository.findByStatutValidationAndStatut(StatutValidation.VALIDE,StatutBien.DISPONIBLE);

        return bienMapper.toResponseDto(biens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponseDto> rechercherParVille(String ville) {
        log.debug("Recuperer les biens valides dans la ville : {}",ville);

        List<Bien> biens = bienRepository.findByStatutValidationAndVilleIgnoreCase(StatutValidation.VALIDE,ville);

        return bienMapper.toResponseDto(biens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponseDto> rechercherParType(TypeBien typeBien) {
        log.debug("Recherche de biens valides de type : {}",typeBien);
        List<Bien> biens= bienRepository.findByStatutValidationAndTypeBien(StatutValidation.VALIDE,typeBien);
        return bienMapper.toResponseDto(biens);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponseDto> rechercheAvancee(String ville, TypeBien typeBien, BigDecimal prixMin, BigDecimal prixMax) {
        log.debug("Recherche avc - Ville: {}, Type: {}, Prix: {}-{}",ville, typeBien, prixMin, prixMax);

        List<Bien> biens = bienRepository.rechercheAvancee(ville,typeBien,prixMin,prixMax,StatutValidation.VALIDE);

        return bienMapper.toResponseDto(biens);
    }

    //stats pour dashboard

    @Override
    public long compterBiensParStatutValidation(StatutValidation StatutValidation) {

        return bienRepository.countByStatutValidation(StatutValidation);
    }
}
