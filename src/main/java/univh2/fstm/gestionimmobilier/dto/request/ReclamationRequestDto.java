package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import univh2.fstm.gestionimmobilier.dto.FileDto;
import univh2.fstm.gestionimmobilier.model.PrioriteReclamation;
import univh2.fstm.gestionimmobilier.model.TypeReclamation;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReclamationRequestDto extends FileDto {

    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    private String reference;

    @NotNull(message = "Le contrat est obligatoire")
    private Long contratId;


    @NotNull(message = "Le type de réclamation est obligatoire")
    private TypeReclamation typeReclamation;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150)
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 5000)
    private String description;

    @NotNull(message = "La priorité est obligatoire")
    private PrioriteReclamation priorite;
}
