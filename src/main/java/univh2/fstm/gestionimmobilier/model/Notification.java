// Notification.java
package univh2.fstm.gestionimmobilier.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "utilisateur")
@EqualsAndHashCode(exclude = "utilisateur")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Personne utilisateur;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(nullable = false)
    private String type; // LOCATION_DEMANDE, LOCATION_RESPONSE, PAYMENT, RECLAMATION, INFO, CONTRACT, VISITE
    
    @Column(nullable = false)
    private boolean lue = false;
    
    @Column(name = "reference_id")
    private Long referenceId; // ID de la demande, paiement, réclamation, etc.
    
    @Column(name = "reference_type")
    private String referenceType; // DEMANDE_LOCATION, PAIEMENT, RECLAMATION, CONTRAT
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;
    
    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}