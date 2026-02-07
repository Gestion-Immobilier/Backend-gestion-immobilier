package univh2.fstm.gestionimmobilier.dto.response;

import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.AuditDto;
import univh2.fstm.gestionimmobilier.model.StatutDemande;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DemandeLocationResponseDto extends AuditDto {

    private Long id;

    // Infos Bien
    private Long bienId;
    private String bienReference;
    private String bienAdresse;
    private String bienVille;

    // Infos Locataire
    private Long locataireId;
    private String locataireNom;
    private String locataireEmail;
    private String locatairePhone;

    private LocalDate dateDebut;
    private Integer dureeContrat;
    private String message;
    private StatutDemande statut;
    private String motifRefus;
    private LocalDateTime dateTraitement;

}