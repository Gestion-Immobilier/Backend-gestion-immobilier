package univh2.fstm.gestionimmobilier.dto.response;

import univh2.fstm.gestionimmobilier.model.StatutDemande;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeLocationResponseDto {

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

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}