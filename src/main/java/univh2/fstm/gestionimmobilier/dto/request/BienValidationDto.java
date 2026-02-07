package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import univh2.fstm.gestionimmobilier.model.StatutValidation;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienValidationDto {
    @NotNull(message = "Le statut de validation est obligatoire")
    private StatutValidation statutValidation;

    @Size(max = 1000, message = "Le motif ne doit pas dépasser 1000 caractères")
    private String motifRejet;  // Obligatoire si REJETE
}
