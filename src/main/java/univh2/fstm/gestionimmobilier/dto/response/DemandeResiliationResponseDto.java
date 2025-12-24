package univh2.fstm.gestionimmobilier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.AuditDto;
import univh2.fstm.gestionimmobilier.model.StatutDemandeResiliation;
import univh2.fstm.gestionimmobilier.model.TypeResiliation;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DemandeResiliationResponseDto extends AuditDto {
    private Long id;
    private Long contratId;
    private Long locataireId;
    private String locataireNom;
    private String locataireEmail;
    private TypeResiliation typeResiliation;
    private LocalDate dateDemandeResiliation;
    private LocalDate dateSouhaiteeResiliation;
    private StatutDemandeResiliation statut;
    private String motif;
    private String commentaireLocataire;
    private LocalDate dateTraitement;
    private String commentaireAdmin;
    private LocalDate dateResiliationEffective;
}
