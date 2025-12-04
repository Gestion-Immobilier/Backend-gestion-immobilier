package univh2.fstm.gestionimmobilier.service.interfaces;

import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationRequestDto;
import univh2.fstm.gestionimmobilier.dto.request.DemandeLocationTraitementDto;
import univh2.fstm.gestionimmobilier.dto.response.DemandeLocationResponseDto;
import univh2.fstm.gestionimmobilier.model.StatutDemande;

import java.util.List;

public interface DemandeLocationService {

    /**
     * Créer une demande de location (LOCATAIRE)
     */
    DemandeLocationResponseDto creerDemande(DemandeLocationRequestDto requestDto);

    /**
     * Récupérer une demande par ID
     */
    DemandeLocationResponseDto getDemandeById(Long id);

    /**
     * Récupérer toutes les demandes (ADMIN)
     */
    List<DemandeLocationResponseDto> getAllDemandes();

    /**
     * Récupérer les demandes d'un locataire
     */
    List<DemandeLocationResponseDto> getMesDemandes(Long locataireId);

    /**
     * Récupérer les demandes en attente (ADMIN)
     */
    List<DemandeLocationResponseDto> getDemandesEnAttente();

    /**
     * Accepter une demande (ADMIN)
     */
    DemandeLocationResponseDto accepterDemande(Long id);

    /**
     * Refuser une demande (ADMIN)
     */
    DemandeLocationResponseDto refuserDemande(Long id, DemandeLocationTraitementDto traitementDto);

    /**
     * Compter les demandes en attente
     */
    long compterDemandesEnAttente();

    /**
     * Récupérer les demandes par statut
     */
    List<DemandeLocationResponseDto> getDemandesByStatut(StatutDemande statut);
}