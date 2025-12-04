package univh2.fstm.gestionimmobilier.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PaymentInitRequest {
    private Long contratId;        // ID du contrat (obligatoire)
    private Long userId;           // ID du locataire qui paie (pour validation)
    private LocalDate moisConcerne; // Mois à payer (format: yyyy-MM-01), optionnel
    private String paymentMethod;  // ex: CREDIT_CARD, VIREMENT, ESPECES
    private String cardNumber;     // Optionnel pour cartes
    private String cardExpiry;     // Optionnel
    private String cardCVV;        // Optionnel

    // Pas besoin de amount car calculé depuis le contrat
    // Pas besoin de propertyId car lié au contrat
}