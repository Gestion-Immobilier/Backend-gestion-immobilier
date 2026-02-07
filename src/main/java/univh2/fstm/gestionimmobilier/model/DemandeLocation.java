package univh2.fstm.gestionimmobilier.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_location")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeLocation extends AuditEntity {

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

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDate dateDebut;

    @Column(name = "duree_contrat", nullable = false)
    @NotNull(message = "La durée du contrat est obligatoire")
    @Min(value = 1, message = "La durée doit être au moins 1 mois")
    @Max(value = 120, message = "La durée ne peut pas dépasser 120 mois")
    private Integer dureeContrat;  // en mois

    @Column(columnDefinition = "TEXT")
    @Size(max = 2000, message = "Le message ne doit pas dépasser 2000 caractères")
    private String message;  // Motivation du locataire

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Le statut est obligatoire")
    @Builder.Default
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    @Column(name = "motif_refus", columnDefinition = "TEXT")
    private String motifRefus;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;


    @OneToOne(mappedBy = "demandeLocation")
    @JsonIgnore
    private Contrat contrat;
}