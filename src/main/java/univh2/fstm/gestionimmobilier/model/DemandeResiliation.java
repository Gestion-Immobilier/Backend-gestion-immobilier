package univh2.fstm.gestionimmobilier.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "demandes_resiliation")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeResiliation extends AuditEntity{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id", nullable = false)
    private Personne locataire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeResiliation typeResiliation;

    @Column(nullable = false)
    private LocalDate dateDemandeResiliation;

    @Column(nullable = false)
    private LocalDate dateSouhaiteeResiliation;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private StatutDemandeResiliation statut;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String commentaireLocataire;

    // Réponse de l'admin
    private LocalDate dateTraitement;

    @Column(columnDefinition = "TEXT")
    private String commentaireAdmin;

    private LocalDate dateResiliationEffective;
}
