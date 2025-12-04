package univh2.fstm.gestionimmobilier.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ← AJOUTEZ CEÇA

    private Contrat contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})  // ← AJOUTEZ CEÇA

    private Personne locataire;

    @Column(name = "montant_loyer", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantLoyer;

    @Column(name = "montant_charges", precision = 10, scale = 2)
    private BigDecimal montantCharges;

    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "mois_concerne", nullable = false)
    private LocalDate moisConcerne; // Premier jour du mois concerné

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Builder.Default
    private String currency = "MAD";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime capturedAt;

    @Column(name = "mode_paiement")
    private String modePaiement;

    @Column(name = "reference_transaction")
    private String referenceTransaction;

    // Méthode pour générer une référence unique
    @PrePersist
    public void generateReference() {
        if (this.reference == null) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            this.reference = "PAY-" + timestamp.substring(timestamp.length() - 8);
        }
    }

    // Méthode utilitaire pour vérifier si le paiement est pour le mois en cours
    public boolean estPourMoisEnCours() {
        LocalDate premierJourMoisCourant = LocalDate.now().withDayOfMonth(1);
        return moisConcerne.equals(premierJourMoisCourant);
    }
}