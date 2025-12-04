package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandeLocationTraitementDto {

    @Size(max = 1000, message = "Le motif ne doit pas dépasser 1000 caractères")
    private String motifRefus;  // Obligatoire si refus
}