package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import univh2.fstm.gestionimmobilier.model.StatutDemandeResiliation;

import java.time.LocalDate;

@Data
public class TraiterDemandeResiliationDto {
    @NotNull(message = "Le statut est obligatoire")
    private StatutDemandeResiliation statut; // ACCEPTEE ou REFUSEE

    private LocalDate dateResiliationEffective; // Si acceptée

    private String commentaireAdmin;
}
