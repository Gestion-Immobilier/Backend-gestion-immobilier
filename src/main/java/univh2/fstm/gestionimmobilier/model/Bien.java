package univh2.fstm.gestionimmobilier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
@Entity
public class Bien extends AuditEntity{
    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_bien", nullable = false, length = 20)
    @NotNull(message = "Le type de bien est obligatoire")
    private TypeBien typeBien;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation", nullable = false, length = 20)
    @NotNull(message = "Le statut de validation est obligatoire")
    private StatutValidation statutValidation ;

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motifRejet;

    @Column(nullable = false)
    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @Column(name = "code_postal", nullable = false, length = 10)
    @NotBlank(message = "Le code postal est obligatoire")
    private String codePostal;

    @Column(nullable = false)
    @NotNull(message = "La surface est obligatoire")
    @DecimalMin(value = "1.0", message = "La surface doit être supérieure à 0")
    private Double surface;

    @Column(name = "nombre_pieces")
    @Min(value = 1, message = "Le nombre de pièces doit être au moins 1")
    private Integer nombrePieces;

    @Column(name = "nombre_chambres")
    @Min(value = 0, message = "Le nombre de chambres ne peut pas être négatif")
    private Integer nombreChambres;

    @Column(name = "nombre_salles_bain")
    @Min(value = 0, message = "Le nombre de salles de bain ne peut pas être négatif")
    private Integer nombreSallesBain;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "loyer_mensuel", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le loyer mensuel est obligatoire")
    @DecimalMin(value = "0.0", message = "Le loyer doit être positif")
    private BigDecimal loyerMensuel;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Les charges doivent être positives")
    private BigDecimal charges;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "La caution doit être positive")
    private BigDecimal caution;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le statut est obligatoire")
    private StatutBien statut;

    @Column(name = "date_acquisition")
    private LocalDate dateAcquisition;

    @ElementCollection
    @CollectionTable(name = "bien_photos", joinColumns = @JoinColumn(name = "bien_id"))
    @Column(name = "photo_url")
    @Builder.Default
    private List<String> photos = new ArrayList<>();

    @Column(name = "meuble")
    @Builder.Default
    private Boolean meuble = false;

    @Column(name = "balcon")
    @Builder.Default
    private Boolean balcon = false;

    @Column(name = "parking")
    @Builder.Default
    private Boolean parking = false;

    @Column(name = "ascenseur")
    @Builder.Default
    private Boolean ascenseur = false;
}
