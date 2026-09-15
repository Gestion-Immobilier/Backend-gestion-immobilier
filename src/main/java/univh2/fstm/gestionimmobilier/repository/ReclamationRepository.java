package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;
import univh2.fstm.gestionimmobilier.model.Reclamation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    
    // ============================
    // Recherche par référence
    // ============================
    Optional<Reclamation> findByReference(String reference);
    
    boolean existsByReference(String reference);
    
    // ============================
    // Recherche par contrat
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.contrat.id = :contratId ORDER BY r.createdOn DESC")
    List<Reclamation> findByContratId(@Param("contratId") Long contratId);
    

    // ============================
    // Recherche par locataire (via contrat)
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.contrat.locataire.id = :locataireId ORDER BY r.createdOn DESC")
    List<Reclamation> findByLocataireId(@Param("locataireId") Long locataireId);
    

    // ============================
    // Recherche par bien (via contrat)
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.contrat.bien.id = :bienId ORDER BY r.createdOn DESC")
    List<Reclamation> findByBienId(@Param("bienId") Long bienId);
    

    
    // ============================
    // Recherche par propriétaire (via contrat.bien)
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.contrat.bien.proprietaire.id = :proprietaireId ORDER BY r.createdOn DESC")
    List<Reclamation> findByProprietaireId(@Param("proprietaireId") Long proprietaireId);

    // ============================
    // Recherche par statut
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.statut = :statut ORDER BY r.createdOn DESC")
    List<Reclamation> findByStatut(@Param("statut") StatutReclamation statut);
    
    List<Reclamation> findByStatutOrderByCreatedOnDesc(StatutReclamation statut);
    
    // ============================
    // Recherche par priorité
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.priorite = :priorite ORDER BY r.createdOn DESC")
    List<Reclamation> findByPriorite(@Param("priorite") PrioriteReclamation priorite);
    
    // ============================
    // Recherche par type
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.type = :type ORDER BY r.createdOn DESC")
    List<Reclamation> findByType(@Param("type") TypeReclamation type);
    
    // ============================
    // Recherche multicritères
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE " +
           "(:statut IS NULL OR r.statut = :statut) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:type IS NULL OR r.typeReclamation = :type) " +
           "ORDER BY r.createdOn DESC")
    List<Reclamation> findByFilters(
            @Param("statut") StatutReclamation statut,
            @Param("priorite") PrioriteReclamation priorite,
            @Param("type") TypeReclamation type
    );
    
    // ============================
    // Recherche par période
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.createdOn BETWEEN :dateDebut AND :dateFin ORDER BY r.createdOn DESC")
    List<Reclamation> findByPeriode(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );
    
    // ============================
    // Comptages
    // ============================
    long countByStatut(StatutReclamation statut);
    
    long countByPriorite(PrioriteReclamation priorite);
    
    long countByType(TypeReclamation type);
    
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.contrat.locataire.id = :locataireId")
    long countByLocataireId(@Param("locataireId") Long locataireId);
    
    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.contrat.bien.id = :bienId")
    long countByBienId(@Param("bienId") Long bienId);
    
    // ============================
    // Réclamations urgentes non traitées
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.priorite = 'URGENTE' " +
           "AND r.statut ='NOUVELLE' or r.statut= 'EN_ATTENTE' " +
           "ORDER BY r.createdOn ASC")
    List<Reclamation> findReclamationsUrgentesNonTraitees();
    
    // ============================
    // Réclamations en retard (> 48h sans prise en charge)
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.statut = 'NOUVELLE' " +
           "AND r.createdOn < :dateLimit " +
           "ORDER BY r.createdOn ASC")
    List<Reclamation> findReclamationsEnRetard(@Param("dateLimit") LocalDateTime dateLimit);
    
    // ============================
    // Statistiques
    // ============================
    @Query("SELECT r.statut, COUNT(r) FROM Reclamation r GROUP BY r.statut")
    List<Object[]> countByStatutGrouped();
    
    @Query("SELECT r.type, COUNT(r) FROM Reclamation r GROUP BY r.type")
    List<Object[]> countByTypeGrouped();
    
    @Query("SELECT r.priorite, COUNT(r) FROM Reclamation r GROUP BY r.priorite")
    List<Object[]> countByPrioriteGrouped();
    
    // ============================
    // Toutes les réclamations avec pagination et tri
    // ============================
    @Query("SELECT r FROM Reclamation r ORDER BY r.createdOn DESC")
    List<Reclamation> findAllReclamations();
    
    // ============================
    // Recherche par mot-clé (titre ou description)
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE " +
           "LOWER(r.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY r.createdOn DESC")
    List<Reclamation> searchByKeyword(@Param("keyword") String keyword);
    
    // ============================
    // Réclamations récentes (dernières 24h)
    // ============================
    @Query("SELECT r FROM Reclamation r WHERE r.createdOn >= :depuis ORDER BY r.createdOn DESC")
    List<Reclamation> findRecentes(@Param("depuis") LocalDateTime depuis);
    
    // ============================
    // Taux de résolution moyen (en jours)
    // ============================
    // ✅ BON - Utiliser une fonction native SQL
    @Query(value = "SELECT AVG(EXTRACT(day FROM (date_resolution - created_on))) " +
            "FROM reclamations " +
            "WHERE statut = 'RESOLUE' AND date_resolution IS NOT NULL",
            nativeQuery = true)
    Double getDelaiResolutionMoyen();
}