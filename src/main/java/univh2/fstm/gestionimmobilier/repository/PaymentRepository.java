package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import univh2.fstm.gestionimmobilier.model.Contrat;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Trouver un paiement par contrat et mois
    Optional<Payment> findByContratAndMoisConcerne(Contrat contrat, LocalDate moisConcerne);

    // Historique des paiements d'un contrat
    List<Payment> findByContratIdOrderByMoisConcerneDesc(Long contratId);

    // Paiements d'un locataire
    List<Payment> findByLocataireIdOrderByCreatedAtDesc(Long locataireId);

    // Paiements par statut
    List<Payment> findByStatus(PaymentStatus status);

    // Vérifier si un mois est déjà payé pour un contrat
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Payment p WHERE p.contrat.id = :contratId " +
            "AND p.moisConcerne = :mois AND p.status = 'CAPTURED'")
    boolean isMoisPaye(@Param("contratId") Long contratId, @Param("mois") LocalDate mois);

    // Récupérer les paiements en retard (date échéance passée et non payés)
    @Query("SELECT p FROM Payment p WHERE p.dateEcheance < CURRENT_DATE " +
            "AND p.status = 'PENDING' AND p.contrat.statut = 'ACTIF'")
    List<Payment> findPaiementsEnRetard();

    // Somme totale des paiements capturés pour un contrat
    @Query("SELECT COALESCE(SUM(p.montantTotal), 0) FROM Payment p " +
            "WHERE p.contrat.id = :contratId AND p.status = 'CAPTURED'")
    BigDecimal getSommePaiementsCaptures(@Param("contratId") Long contratId);
}