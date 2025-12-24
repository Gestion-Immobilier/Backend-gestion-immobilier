package univh2.fstm.gestionimmobilier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Entity
public class MaintenanceTicket extends AuditEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reclamation_id", nullable = false)
    private Reclamation reclamation;


    @Column(nullable = false, length = 150)
    private String technicien; // nom ou société

    @NotNull
    @Column(name = "date_intervention_prevue")
    private LocalDateTime dateInterventionPrevue;

    @Column(name = "date_intervention")
    private LocalDateTime dateIntervention;

    @Min(value = 1, message = "La durée doit être au moins 1 minute")
    @Column(name = "duree_intervention")
    private Integer dureeIntervention; // en minutes


    @DecimalMin(value = "0.0", message = "Le coût doit être positif")
    @Column(precision = 10, scale = 2)
    private BigDecimal cout;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutMaintenance statut;

    @Column(columnDefinition = "TEXT")
    private String remarques;



}
