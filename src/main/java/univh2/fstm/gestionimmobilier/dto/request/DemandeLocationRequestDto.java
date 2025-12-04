package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeLocationRequestDto {

    @NotNull(message = "Le bien est obligatoire")
    private Long bienId;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDate dateDebut;

    @NotNull(message = "La durée du contrat est obligatoire")
    @Min(value = 1, message = "La durée doit être au moins 1 mois")
    @Max(value = 120, message = "La durée ne peut pas dépasser 120 mois")
    private Integer dureeContrat;

    @Size(max = 2000, message = "Le message ne doit pas dépasser 2000 caractères")
    private String message;
}