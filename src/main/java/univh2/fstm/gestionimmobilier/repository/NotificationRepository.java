// NotificationRepository.java
package univh2.fstm.gestionimmobilier.repository;

import univh2.fstm.gestionimmobilier.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUtilisateurIdOrderByDateEnvoiDesc(Long utilisateurId);
    
    List<Notification> findByUtilisateurIdAndLueFalseOrderByDateEnvoiDesc(Long utilisateurId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    long countByUtilisateurIdAndLueFalse(@Param("utilisateurId") Long utilisateurId);
    
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :utilisateurId AND n.type = :type ORDER BY n.dateEnvoi DESC")
    List<Notification> findByUtilisateurIdAndType(@Param("utilisateurId") Long utilisateurId, 
                                                  @Param("type") String type);
    
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true, n.dateLecture = :dateLecture WHERE n.id = :id")
    void marquerCommeLue(@Param("id") Long id, @Param("dateLecture") LocalDateTime dateLecture);
    
    @Modifying
    @Query("UPDATE Notification n SET n.lue = true, n.dateLecture = :dateLecture WHERE n.utilisateur.id = :utilisateurId AND n.lue = false")
    void marquerToutCommeLu(@Param("utilisateurId") Long utilisateurId, 
                           @Param("dateLecture") LocalDateTime dateLecture);
    
    void deleteByUtilisateurId(Long utilisateurId);
    
    @Query("SELECT n FROM Notification n WHERE n.utilisateur.id = :utilisateurId AND n.dateEnvoi >= :dateDebut ORDER BY n.dateEnvoi DESC")
    List<Notification> findRecentByUtilisateurId(@Param("utilisateurId") Long utilisateurId,
                                                @Param("dateDebut") LocalDateTime dateDebut);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.utilisateur.id = :utilisateurId AND n.dateEnvoi < :dateLimite")
    void deleteOldNotifications(@Param("utilisateurId") Long utilisateurId,
                               @Param("dateLimite") LocalDateTime dateLimite);
}