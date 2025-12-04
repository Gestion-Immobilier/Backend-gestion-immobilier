package univh2.fstm.gestionimmobilier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;



@Entity
@Table(name = "contrats")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Contrat extends FileEntity{

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bien_id", nullable = false)
    @JsonIgnore
    @NotNull(message = "Le bien est obligatoire")
    private Bien bien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id", nullable = false)
    @JsonIgnore
    @NotNull(message = "Le locataire est obligatoire")
    private Personne locataire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_location_id")
    @JsonIgnore
    private DemandeLocation demandeLocation;  // Traçabilité

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDate dateFin;

    @Column(name = "loyer_mensuel", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Le loyer mensuel est obligatoire")
    @DecimalMin(value = "0.01", message = "Le loyer doit être supérieur à 0")
    private BigDecimal loyerMensuel;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "Les charges doivent être positives")
    private BigDecimal charges;

    @Column(precision = 10, scale = 2)
    @DecimalMin(value = "0.0", message = "La caution doit être positive")
    private BigDecimal caution;

    @Column(name = "jour_paiement")
    @Min(value = 1, message = "Le jour de paiement doit être entre 1 et 28")
    @Max(value = 28, message = "Le jour de paiement doit être entre 1 et 28")
    private Integer jourPaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", nullable = false, length = 20)
    @NotNull(message = "Le type de contrat est obligatoire")
    private TypeContrat typeContrat;

    @Column(name = "duree_contrat", nullable = false)
    @NotNull(message = "La durée du contrat est obligatoire")
    @Min(value = 1, message = "La durée doit être au moins 1 mois")
    private Integer dureeContrat;  // en mois

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le statut est obligatoire")
    @Builder.Default
    private StatutContrat statut = StatutContrat.ACTIF;

    @Column(name = "date_signature")
    private LocalDate dateSignature;

    @Column(name = "clauses_particulieres", columnDefinition = "TEXT")
    private String clausesParticulieres;

    /* Relations
    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Paiement> paiements = new ArrayList<>();

    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<DemandeResiliation> demandesResiliation = new ArrayList<>();

     */
}
