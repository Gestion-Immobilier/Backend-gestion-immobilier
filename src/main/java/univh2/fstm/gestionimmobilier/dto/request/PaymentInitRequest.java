package univh2.fstm.gestionimmobilier.dto.request;

import lombok.Data;

@Data
public class PaymentInitRequest {
    private Long userId;
   private Long propertyId;
    private double amount;
    private String paymentMethod; // ex: CREDIT_CARD
    private String cardNumber;    // 16 chiffres
    private String cardExpiry;    // MM/YY
    private String cardCVV;       // 3 chiffres
}
