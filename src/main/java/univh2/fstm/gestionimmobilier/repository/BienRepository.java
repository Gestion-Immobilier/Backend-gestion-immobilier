package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import univh2.fstm.gestionimmobilier.model.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BienRepository extends JpaRepository<Bien, Long> {

    // Gardez TOUTES les méthodes existantes
    Optional<Bien> findBienByReference(String reference);
    List<Bien> findByStatutValidationAndStatut(StatutValidation statutValidation, StatutBien statut);
    List<Bien> findByStatutValidationAndVilleIgnoreCase(StatutValidation statutValidation, String ville);

    @Query("SELECT b FROM Bien b WHERE b.statutValidation = :statutValidation " +
            "AND b.loyerMensuel BETWEEN :prixMin AND :prixMax")
    List<Bien> findBiensValidesByPrix(
            @Param("statutValidation") StatutValidation statutValidation,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax
    );

    List<Bien> findByStatutValidation(StatutValidation statutValidation);
    long countByStatutValidation(StatutValidation statutValidation);
    boolean existsByReference(String reference);
    Optional<Bien> findByReference(String reference);
    List<Bien> findByStatutValidationAndTypeBien(StatutValidation statutValidation, TypeBien typeBien);
    List<Bien> findByProprietaireId(Long proprietaireId);
    long countByProprietaireId(Long proprietaireId);

    // ========== CORRECTION de la méthode problématique ==========
    // Version corrigée sans LOWER() - garde l'ancienne signature
    @Query("SELECT b FROM Bien b WHERE " +
            "(:ville IS NULL OR b.ville LIKE %:ville%) " + // REMPLACÉ: Enlevé LOWER()
            "AND (:typeBien IS NULL OR b.typeBien = :typeBien) " +
            "AND (:prixMin IS NULL OR b.loyerMensuel >= :prixMin) " +
            "AND (:prixMax IS NULL OR b.loyerMensuel <= :prixMax) " +
            "AND b.statutValidation = :statutValidation")
    List<Bien> rechercheAvancee(
            @Param("ville") String ville,
            @Param("typeBien") TypeBien typeBien,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statutValidation") StatutValidation statutValidation
    );

    // ========== AJOUT: Nouvelle méthode pour recherche insensible à la casse ==========
    @Query("SELECT b FROM Bien b WHERE " +
            "(:ville IS NULL OR UPPER(b.ville) LIKE UPPER(CONCAT('%', :ville, '%'))) " +
            "AND (:typeBien IS NULL OR b.typeBien = :typeBien) " +
            "AND (:prixMin IS NULL OR b.loyerMensuel >= :prixMin) " +
            "AND (:prixMax IS NULL OR b.loyerMensuel <= :prixMax) " +
            "AND b.statutValidation = :statutValidation")
    List<Bien> rechercheAvanceeInsensible(
            @Param("ville") String ville,
            @Param("typeBien") TypeBien typeBien,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statutValidation") StatutValidation statutValidation
    );

    // ========== AJOUT: Méthode alternative simple ==========
    @Query("SELECT b FROM Bien b WHERE " +
            "b.statutValidation = :statutValidation " +
            "AND (:ville IS NULL OR b.ville LIKE CONCAT('%', :ville, '%')) " +
            "AND (:typeBien IS NULL OR b.typeBien = :typeBien)")
    List<Bien> rechercheSimple(
            @Param("ville") String ville,
            @Param("typeBien") TypeBien typeBien,
            @Param("statutValidation") StatutValidation statutValidation
    );
}