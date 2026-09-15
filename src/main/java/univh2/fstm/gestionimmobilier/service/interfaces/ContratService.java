package univh2.fstm.gestionimmobilier.service.interfaces;

import univh2.fstm.gestionimmobilier.dto.request.ContratRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ContratResponseDto;
import univh2.fstm.gestionimmobilier.model.StatutContrat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface ContratService {

    /**
     * Créer un contrat (ADMIN uniquement)
     */
    ContratResponseDto creerContrat(ContratRequestDto requestDto, MultipartFile documentPdf);

    /**
     * Récupérer un contrat par ID
     */
    ContratResponseDto getContratById(Long id);

    /**
     * Récupérer un contrat par référence
     */
    ContratResponseDto getContratByReference(String reference);

    /**
     * Récupérer tous les contrats (ADMIN)
     */
    List<ContratResponseDto> getAllContrats();
    Page<ContratResponseDto> getAllContratsPaged(Pageable pageable);

    /**
     * Récupérer les contrats d'un locataire
     */
    List<ContratResponseDto> getContratsLocataire(Long locataireId);
    Page<ContratResponseDto> getContratsLocatairePaged(Long locataireId, Pageable pageable);

    /**
     * Récupérer les contrats d'un bien
     */
    List<ContratResponseDto> getContratsBien(Long bienId);
    Page<ContratResponseDto> getContratsBienPaged(Long bienId, Pageable pageable);

    /**
     * Récupérer les contrats d'un propriétaire
     */
    List<ContratResponseDto> getContratsProprietaire(Long proprietaireId);
    Page<ContratResponseDto> getContratsProprietairePaged(Long proprietaireId, Pageable pageable);

    /**
     * Récupérer les contrats par statut
     */
    List<ContratResponseDto> getContratsByStatut(StatutContrat statut);
    Page<ContratResponseDto> getContratsByStatutPaged(StatutContrat statut, Pageable pageable);

    /**
     * Mettre à jour un contrat
     */
    ContratResponseDto updateContrat(Long id, ContratRequestDto requestDto, MultipartFile documentPdf);

    /**
     * Résilier un contrat
     */
    ContratResponseDto resilierContrat(Long id);

    /**
     * Supprimer un contrat
     */
    void deleteContrat(Long id);

    /**
     * Télécharger le document PDF d'un contrat
     */
    InputStream downloadDocumentContrat(Long id);

    /**
     * Obtenir le content type du document
     */
    String getDocumentContentType(Long id);

    /**
     * Vérifier et marquer les contrats expirés
     */
    void verifierContratsExpires();
}