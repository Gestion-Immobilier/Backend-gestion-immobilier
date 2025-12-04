package univh2.fstm.gestionimmobilier.dto.response;

import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.FileDto;
import univh2.fstm.gestionimmobilier.model.StatutContrat;
import univh2.fstm.gestionimmobilier.model.TypeContrat;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContratResponseDto extends FileDto {

    private Long id;
    private String reference;

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

    // Infos Propriétaire
    private Long proprietaireId;
    private String proprietaireNom;
    private String proprietaireEmail;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private BigDecimal loyerMensuel;
    private BigDecimal charges;
    private BigDecimal caution;
    private Integer jourPaiement;
    private TypeContrat typeContrat;
    private Integer dureeContrat;
    private StatutContrat statut;
    private LocalDate dateSignature;
    private String clausesParticulieres;

}