package univh2.fstm.gestionimmobilier.repository;

import univh2.fstm.gestionimmobilier.model.DemandeLocation;
import univh2.fstm.gestionimmobilier.model.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeLocationRepository extends JpaRepository<DemandeLocation, Long> {

    /**
     * Récupère toutes les demandes par statut
     */
    List<DemandeLocation> findByStatut(StatutDemande statut);

    /**
     * Récupère les demandes d'un locataire
     */
    List<DemandeLocation> findByLocataireId(Long locataireId);

    /**
     * Récupère les demandes pour un bien spécifique
     */
    List<DemandeLocation> findByBienId(Long bienId);

    /**
     * Vérifie si un locataire a déjà une demande EN_ATTENTE pour ce bien
     */
    boolean existsByLocataireIdAndBienIdAndStatut(Long locataireId, Long bienId, StatutDemande statut);

    /**
     * Compte les demandes en attente
     */
    long countByStatut(StatutDemande statut);

    /**
     * Récupère les demandes d'un bien avec statut EN_ATTENTE
     */
    @Query("SELECT d FROM DemandeLocation d WHERE d.bien.id = :bienId AND d.statut = 'EN_ATTENTE' ORDER BY d.createdOn ASC")
    List<DemandeLocation> findDemandesEnAttenteByBien(@Param("bienId") Long bienId);
}