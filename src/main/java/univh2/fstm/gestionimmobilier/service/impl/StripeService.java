package univh2.fstm.gestionimmobilier.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Payment;

import java.math.BigDecimal;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("🔑 Stripe SDK initialisé");
    }

    /**
     * Crée une session de paiement Stripe Checkout.
     * Retourne l'URL vers laquelle rediriger le locataire.
     */
    public String creerSessionPaiement(Payment payment) throws StripeException {
        // Le montant en Stripe est exprimé en centimes (ex: 150.00 MAD → 15000)
        long montantCentimes = payment.getMontantTotal()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        String nomProduit = String.format("Loyer %s/%s — Contrat %s",
                payment.getMoisConcerne().getMonthValue(),
                payment.getMoisConcerne().getYear(),
                payment.getContrat().getReference());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("mad")
                                                .setUnitAmount(montantCentimes)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(nomProduit)
                                                                .setDescription("Référence paiement: " + payment.getReference())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                // Métadonnée cruciale : permet de retrouver le Payment depuis le webhook
                .putMetadata("paymentId", payment.getId().toString())
                .putMetadata("paymentReference", payment.getReference())
                .build();

        Session session = Session.create(params);
        log.info("✅ Session Stripe créée: {} pour paiement: {}", session.getId(), payment.getReference());
        return session.getId();
    }

    /**
     * Récupère l'URL de checkout à partir d'un session ID.
     */
    public String getCheckoutUrl(String sessionId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        return session.getUrl();
    }
}
