package univh2.fstm.gestionimmobilier.repository;

import univh2.fstm.gestionimmobilier.model.Contrat;
import univh2.fstm.gestionimmobilier.model.StatutContrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    /**
     * Trouve un contrat par référence
     */
    Optional<Contrat> findByReference(String reference);

    /**
     * Vérifie si une référence existe
     */
    boolean existsByReference(String reference);

    /**
     * Trouve tous les contrats d'un bien
     */
    List<Contrat> findByBienId(Long bienId);

    /**
     * Trouve tous les contrats d'un locataire
     */
    List<Contrat> findByLocataireId(Long locataireId);

    /**
     * Trouve les contrats par statut
     */
    List<Contrat> findByStatut(StatutContrat statut);

    /**
     * Vérifie si un bien a un contrat ACTIF
     */
    boolean existsByBienIdAndStatut(Long bienId, StatutContrat statut);

    /**
     * Trouve le contrat ACTIF d'un bien
     */
    Optional<Contrat> findByBienIdAndStatut(Long bienId, StatutContrat statut);

    /**
     * Compte les contrats par statut
     */
    long countByStatut(StatutContrat statut);

    /**
     * Trouve les contrats expirés (dateFin dépassée et statut ACTIF)
     */
    @Query("SELECT c FROM Contrat c WHERE c.dateFin < :today AND c.statut = 'ACTIF'")
    List<Contrat> findContratsExpires(@Param("today") LocalDate today);

    /**
     * Trouve les contrats d'un propriétaire (via le bien)
     */
    @Query("SELECT c FROM Contrat c WHERE c.bien.proprietaire.id = :proprietaireId")
    List<Contrat> findByProprietaireId(@Param("proprietaireId") Long proprietaireId);
}