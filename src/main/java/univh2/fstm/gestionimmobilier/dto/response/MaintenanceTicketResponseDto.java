package univh2.fstm.gestionimmobilier.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.AuditDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.StatutMaintenance;
import univh2.fstm.gestionimmobilier.model.StatutReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MaintenanceTicketResponseDto extends AuditDto {

    private Long id;
    private Long reclamationId;
    private TypeReclamation typeReclamation;
    private PrioriteReclamation priorite;
    private StatutReclamation statutReclamation;

    private String technicien;
    private LocalDateTime dateInterventionPrevue;
    private LocalDateTime dateIntervention;
    private Integer dureeIntervention;
    private BigDecimal cout;
    private String description;
    private StatutMaintenance statut;
    private String remarques;
}
