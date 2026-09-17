package univh2.fstm.gestionimmobilier.model;

public enum PaymentStatus {
    PENDING,
    CAPTURED,
    PAID,      // Confirmé par Stripe webhook
    FAILED,    // Échec Stripe
    CANCELLED
}
