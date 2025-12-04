package univh2.fstm.gestionimmobilier.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;
import univh2.fstm.gestionimmobilier.dto.FileDto;
import univh2.fstm.gestionimmobilier.model.TypeContrat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Données pour créer un contrat")
public class ContratRequestDto extends FileDto {

    @Schema(description = "ID de la demande de location (optionnel)")
    private Long demandeLocationId;  // Optionnel (traçabilité)

    @NotNull(message = "Le bien est obligatoire")
    @Schema(description = "ID du bien")
    private Long bienId;

    @NotNull(message = "Le locataire est obligatoire")
    @Schema(description = "ID du locataire",example = "5")
    private Long locataireId;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être aujourd'hui ou dans le futur")
    @Schema(description = "Date de début du contrat",example = "2025-01-01")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @NotNull(message = "Le loyer mensuel est obligatoire")
    @DecimalMin(value = "0.01", message = "Le loyer doit être supérieur à 0")
    private BigDecimal loyerMensuel;

    @DecimalMin(value = "0.0", message = "Les charges doivent être positives")
    private BigDecimal charges;

    @DecimalMin(value = "0.0", message = "La caution doit être positive")
    private BigDecimal caution;

    @Min(value = 1, message = "Le jour de paiement doit être entre 1 et 28")
    @Max(value = 28, message = "Le jour de paiement doit être entre 1 et 28")
    private Integer jourPaiement;

    @NotNull(message = "Le type de contrat est obligatoire")
    private TypeContrat typeContrat;

    @NotNull(message = "La durée du contrat est obligatoire")
    @Min(value = 1, message = "La durée doit être au moins 1 mois")
    @Max(value = 120, message = "La durée ne peut pas dépasser 120 mois")
    private Integer dureeContrat;

    private LocalDate dateSignature;

    @Size(max = 5000, message = "Les clauses particulières ne doivent pas dépasser 5000 caractères")
    private String clausesParticulieres;
//
//     Le fichier PDF sera envoyé séparément via MultipartFile
//    private MultipartFile document;
}