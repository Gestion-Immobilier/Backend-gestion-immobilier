package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.AuditDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class MaintenanceTicketRequestDto extends AuditDto {

    @NotNull(message = "La réclamation est obligatoire")
    private Long reclamationId;

    @NotNull(message = "Le technicien est obligatoire")
    private String technicien;

    @NotNull(message = "La date d'intervention prévue est obligatoire")
    private LocalDateTime dateInterventionPrevue;

    private LocalDateTime dateIntervention;

    @Min(1)
    private Integer dureeIntervention;

    @DecimalMin("0.0")
    private BigDecimal cout;

    private String description;
    private String remarques;
}
