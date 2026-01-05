package univh2.fstm.gestionimmobilier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.AuditDto;
import univh2.fstm.gestionimmobilier.dto.FileDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReclamationResponseDto extends AuditDto {
    private Long id;
    private String reference;

    //Infos Contrat
    private ContratLightDto contrat;


    private TypeReclamation typeReclamation;
    private String titre;
    private String description;

    private PrioriteReclamation priorite;
    private StatutReclamation statut;
    private LocalDateTime dateResolution;

    private List<String> photos;
}
