package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import univh2.fstm.gestionimmobilier.model.DemandeResiliation;
import univh2.fstm.gestionimmobilier.model.StatutDemandeResiliation;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeResiliationRepository extends JpaRepository<DemandeResiliation, Long> {

    // Demandes par locataire
    @Query("SELECT d FROM DemandeResiliation d WHERE d.locataire.id = :locataireId ORDER BY d.dateDemandeResiliation DESC")
    List<DemandeResiliation> findByLocataireId(@Param("locataireId") Long locataireId);

    // Demandes par contrat
    @Query("SELECT d FROM DemandeResiliation d WHERE d.contrat.id = :contratId ORDER BY d.dateDemandeResiliation DESC")
    List<DemandeResiliation> findByContratId(@Param("contratId") Long contratId);

    // Demandes par statut
    List<DemandeResiliation> findByStatut(StatutDemandeResiliation statut);

    // Toutes les demandes (pour admin)
    @Query("SELECT d FROM DemandeResiliation d ORDER BY d.dateDemandeResiliation DESC")
    List<DemandeResiliation> findAllDemandes();

    // ✅ CORRIGÉ : Vérifier si un contrat a une demande en cours
    @Query("SELECT d FROM DemandeResiliation d WHERE d.contrat.id = :contratId " +
            "AND (d.statut = 'EN_ATTENTE' OR d.statut = 'EN_COURS_EXAMEN')")
    Optional<DemandeResiliation> findDemandeEnCoursByContrat(@Param("contratId") Long contratId);

    // Compter les demandes en attente
    long countByStatut(StatutDemandeResiliation statut);

    // ✅ CORRIGÉ : Vérifier si le locataire a déjà une demande en cours pour ce contrat
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DemandeResiliation d " +
            "WHERE d.locataire.id = :locataireId " +
            "AND d.contrat.id = :contratId " +
            "AND (d.statut = 'EN_ATTENTE' OR d.statut = 'EN_COURS_EXAMEN')")
    boolean existsDemandeEnCoursByLocataireAndContrat(
            @Param("locataireId") Long locataireId,
            @Param("contratId") Long contratId
    );
}
