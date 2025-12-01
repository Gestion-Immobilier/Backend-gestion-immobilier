package univh2.fstm.gestionimmobilier.dto.request;

import lombok.Data;

@Data
public class PaymentInitRequest {
    private Long userId;
    private Long propertyId;
    private double amount;
}
