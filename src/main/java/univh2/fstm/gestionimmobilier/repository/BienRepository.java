package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.StatutValidation;
import univh2.fstm.gestionimmobilier.model.TypeBien;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BienRepository extends JpaRepository<Bien, Long> {

    Optional<Bien> findBienByReference(String reference);
    List<Bien> findByStatutValidationAndStatut(StatutValidation statutValidation,StatutBien statut);
    Page<Bien> findByStatutValidationAndStatut(StatutValidation statutValidation, StatutBien statut, Pageable pageable);
    
    List<Bien> findByStatutValidationAndVilleIgnoreCase(StatutValidation statutValidation, String ville);
    Page<Bien> findByStatutValidationAndVilleIgnoreCase(StatutValidation statutValidation, String ville, Pageable pageable);


    @Query("SELECT b FROM Bien b WHERE b.statutValidation = :statutValidation " +
            "AND b.loyerMensuel BETWEEN :prixMin AND :prixMax")
    List<Bien> findBiensValidesByPrix(
            @Param("statutValidation") StatutValidation statutValidation,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax
    );

    // Pour les agents

    List<Bien> findByStatutValidation(StatutValidation statutValidation);
    long countByStatutValidation(StatutValidation statutValidation);

    boolean existsByReference(String reference);
    Optional<Bien> findByReference(String reference);



    @Query("SELECT b FROM Bien b WHERE " +
            "(:ville IS NULL OR LOWER(b.ville) LIKE LOWER(CONCAT('%', :ville, '%'))) " +
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

    @Query("SELECT b FROM Bien b WHERE " +
            "(:ville IS NULL OR LOWER(b.ville) LIKE LOWER(CONCAT('%', :ville, '%'))) " +
            "AND (:typeBien IS NULL OR b.typeBien = :typeBien) " +
            "AND (:prixMin IS NULL OR b.loyerMensuel >= :prixMin) " +
            "AND (:prixMax IS NULL OR b.loyerMensuel <= :prixMax) " +
            "AND b.statutValidation = :statutValidation")
    Page<Bien> rechercheAvanceePageable(
            @Param("ville") String ville,
            @Param("typeBien") TypeBien typeBien,
            @Param("prixMin") BigDecimal prixMin,
            @Param("prixMax") BigDecimal prixMax,
            @Param("statutValidation") StatutValidation statutValidation,
            Pageable pageable
    );

    List<Bien> findByStatutValidationAndTypeBien( StatutValidation statutValidation, TypeBien typeBien);

    List<Bien> findByProprietaireId(Long proprietaireId);
    long countByProprietaireId(Long proprietaireId);

    // ================== RECHERCHES SPATIALES POSTGIS ================== //

    @Query(value = """
        SELECT * FROM bien b
        WHERE b.statut_validation = 'VALIDE' 
        AND b.statut = 'DISPONIBLE'
        AND ST_DWithin(
            b.localisation::geography,
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
            :rayonMetres
        )
        ORDER BY ST_Distance(
            b.localisation::geography,
            ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
        ) ASC
        """, nativeQuery = true)
    List<Bien> findBiensDansRayon(
        @Param("lat") double latitude,
        @Param("lon") double longitude,
        @Param("rayonMetres") double rayonMetres
    );

    @Query(value = """
        SELECT * FROM bien b
        WHERE b.statut_validation = 'VALIDE'
        AND ST_Within(
            b.localisation,
            ST_MakeEnvelope(:lonMin, :latMin, :lonMax, :latMax, 4326)
        )
        """, nativeQuery = true)
    List<Bien> findBiensDansZone(
        @Param("lonMin") double lonMin,
        @Param("latMin") double latMin,
        @Param("lonMax") double lonMax,
        @Param("latMax") double latMax
    );
}
