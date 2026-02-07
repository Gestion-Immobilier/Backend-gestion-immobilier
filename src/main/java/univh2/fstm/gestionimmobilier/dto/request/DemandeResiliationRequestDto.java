package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.model.TypeResiliation;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DemandeResiliationRequestDto {
    @NotNull(message = "Le contrat est obligatoire")
    private Long contratId;

    @NotNull(message = "Le type de résiliation est obligatoire")
    private TypeResiliation typeResiliation;

    @NotNull(message = "La date souhaitée de résiliation est obligatoire")
    private LocalDate dateSouhaiteeResiliation;

    @NotNull(message = "Le motif est obligatoire")
    private String motif;

    private String commentaireLocataire;
}
