package univh2.fstm.gestionimmobilier.service.interfaces;


import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.request.ReclamationRequestDto;
import univh2.fstm.gestionimmobilier.dto.response.ReclamationResponseDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;

import java.util.List;
import java.util.Map;

public interface ReclamationService {
    
    // ============================
    // CRUD de base
    // ============================
    
    /**
     * Créer une nouvelle réclamation
     */
    ReclamationResponseDto creerReclamation(
            ReclamationRequestDto requestDto,
            List<MultipartFile> photos
    );
    
    /**
     * Récupérer une réclamation par ID
     */
    ReclamationResponseDto getReclamationById(Long id);
    
    /**
     * Récupérer une réclamation par référence
     */
    ReclamationResponseDto getReclamationByReference(String reference);
    
    /**
     * Mettre à jour une réclamation
     */
    ReclamationResponseDto updateReclamation(Long id, ReclamationRequestDto requestDto);
    
    /**
     * Supprimer une réclamation
     */
    void deleteReclamation(Long id);
    
    // ============================
    // Recherches et filtres
    // ============================
    
    /**
     * Récupérer toutes les réclamations avec pagination
     */
    List<ReclamationResponseDto> getAllReclamations();
    
    /**
     * Récupérer les réclamations d'un contrat
     */
    List<ReclamationResponseDto> getReclamationsByContrat(Long contratId);
    
    /**
     * Récupérer les réclamations d'un locataire
     */
    List<ReclamationResponseDto> getReclamationsByLocataire(Long locataireId);
    
    /**
     * Récupérer les réclamations d'un bien
     */
    List<ReclamationResponseDto> getReclamationsByBien(Long bienId);
    
    /**
     * Récupérer les réclamations d'un propriétaire
     */
    List<ReclamationResponseDto> getReclamationsByProprietaire(Long proprietaireId);
    
    /**
     * Récupérer mes réclamations (locataire connecté)
     */
    List<ReclamationResponseDto> getMesReclamations();
    
    /**
     * Filtrer les réclamations par critères
     */
    List<ReclamationResponseDto> filtrerReclamations(
            StatutReclamation statut,
            PrioriteReclamation priorite,
            TypeReclamation type
    );
    
    /**
     * Rechercher par mot-clé
     */
    List<ReclamationResponseDto> rechercherReclamations(String keyword);
    
    // ============================
    // Gestion du statut
    // ============================
    
    /**
     * Changer le statut d'une réclamation
     */
    ReclamationResponseDto changerStatut(Long id, StatutReclamation nouveauStatut);
    
    /**
     * Prendre en charge une réclamation
     */
    ReclamationResponseDto prendreEnCharge(Long id, String commentaire);
    
    /**
     * Résoudre une réclamation
     */
    ReclamationResponseDto resoudreReclamation(Long id, String solution);
    
    /**
     * Fermer une réclamation
     */
    ReclamationResponseDto fermerReclamation(Long id);
    
    /**
     * Annuler une réclamation (par le locataire)
     */
    void annulerReclamation(Long id);
    
    // ============================
    // Gestion des photos
    // ============================
    
    /**
     * Ajouter des photos à une réclamation existante
     */
    ReclamationResponseDto ajouterPhotos(Long id, List<MultipartFile> photos);
    
    /**
     * Supprimer une photo d'une réclamation
     */
    void supprimerPhoto(Long reclamationId, String photoUrl);
    
    // ============================
    // Réclamations spéciales
    // ============================
    
    /**
     * Récupérer les réclamations urgentes non traitées
     */
    List<ReclamationResponseDto> getReclamationsUrgentes();
    
    /**
     * Récupérer les réclamations en retard (> 48h)
     */
    List<ReclamationResponseDto> getReclamationsEnRetard();
    
    /**
     * Récupérer les réclamations récentes (24h)
     */
    List<ReclamationResponseDto> getReclamationsRecentes();
    
    // ============================
    // Statistiques
    // ============================
    
    /**
     * Nombre total de réclamations
     */
    long getTotalReclamations();
    
    /**
     * Nombre de réclamations par statut
     */
    long countByStatut(StatutReclamation statut);
    
    /**
     * Répartition des réclamations par statut
     */
    Map<StatutReclamation, Long> getStatistiquesParStatut();
    
    /**
     * Répartition des réclamations par type
     */
    Map<TypeReclamation, Long> getStatistiquesParType();
    
    /**
     * Répartition des réclamations par priorité
     */
    Map<PrioriteReclamation, Long> getStatistiquesParPriorite();
    
    /**
     * Délai moyen de résolution (en jours)
     */
    Double getDelaiResolutionMoyen();
}