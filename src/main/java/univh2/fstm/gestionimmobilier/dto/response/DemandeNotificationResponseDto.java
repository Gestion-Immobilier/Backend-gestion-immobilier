// DemandeNotificationResponseDto.java
package univh2.fstm.gestionimmobilier.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeNotificationResponseDto {
    
    private Long id;
    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurEmail;
    
    private String titre;
    private String message;
    private String type;
    private boolean lue;
    
    private Long referenceId;
    private String referenceType;
    private Map<String, Object> metadata;
    
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLecture;
    private LocalDateTime dateCreation;
}