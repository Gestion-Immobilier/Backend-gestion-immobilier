package univh2.fstm.gestionimmobilier.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import univh2.fstm.gestionimmobilier.model.StatutBien;
import univh2.fstm.gestionimmobilier.model.TypeBien;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienRequestDto {

    @Size(max = 50, message = "La référence ne doit pas dépasser 50 caractères")
    private String reference;

    @NotNull(message = "Le type de bien est obligatoire")
    private TypeBien typeBien;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String adresse;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100, message = "La ville ne doit pas dépasser 100 caractères")
    private String ville;

    @NotBlank(message = "Le code postal est obligatoire")
    @Pattern(regexp = "^[0-9]{5}$", message = "Le code postal doit contenir 5 chiffres")
    private String codePostal;

    @NotNull(message = "La surface est obligatoire")
    @DecimalMin(value = "1.0", message = "La surface doit être supérieure à 0")
    private Double surface;

    @Min(value = 1, message = "Le nombre de pièces doit être au moins 1")
    private Integer nombrePieces;

    @Min(value = 0, message = "Le nombre de chambres ne peut pas être négatif")
    private Integer nombreChambres;

    @Min(value = 0, message = "Le nombre de salles de bain ne peut pas être négatif")
    private Integer nombreSallesBain;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères")
    private String description;

    @NotNull(message = "Le loyer mensuel est obligatoire")
    @DecimalMin(value = "0.01", message = "Le loyer doit être supérieur à 0")
    private BigDecimal loyerMensuel;

    @DecimalMin(value = "0.0", message = "Les charges doivent être positives")
    private BigDecimal charges;

    @DecimalMin(value = "0.0", message = "La caution doit être positive")
    private BigDecimal caution;

    @NotNull(message = "Le statut est obligatoire")
    private StatutBien statut;

    private LocalDate dateAcquisition;

    private List<String> photos;

    private Boolean meuble;

    private Boolean balcon;

    private Boolean parking;

    private Boolean ascenseur;

    // Note : proprietaireId sera ajouté quand ton binôme aura créé Proprietaire
    // private UUID proprietaireId;
}
