// DemandeNotificationRequestDto.java
package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeNotificationRequestDto {
    
    @NotNull(message = "L'utilisateur est obligatoire")
    private Long utilisateurId;
    
    @NotBlank(message = "Le titre est obligatoire")
    private String titre;
    
    @NotBlank(message = "Le message est obligatoire")
    private String message;
    
    @NotBlank(message = "Le type est obligatoire")
    private String type; // LOCATION_DEMANDE, LOCATION_RESPONSE, PAYMENT, RECLAMATION, INFO
    
    private Map<String, Object> metadata; // Données supplémentaires
    private Long referenceId; // ID de la demande, paiement, réclamation, etc.
    private String referenceType; // DEMANDE_LOCATION, PAIEMENT, RECLAMATION
}