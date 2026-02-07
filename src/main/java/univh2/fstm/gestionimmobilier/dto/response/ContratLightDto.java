package univh2.fstm.gestionimmobilier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.FileDto;
import univh2.fstm.gestionimmobilier.model.StatutContrat;
import univh2.fstm.gestionimmobilier.model.TypeContrat;

import java.math.BigDecimal;
import java.time.LocalDate;


@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ContratLightDto extends FileDto {

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
    private StatutContrat statut;

}
