package univh2.fstm.gestionimmobilier.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.service.impl.NotificationService;
import univh2.fstm.gestionimmobilier.service.impl.PaymentService;
import univh2.fstm.gestionimmobilier.service.impl.ReceiptService;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Stripe Webhook", description = "Endpoint réservé à Stripe pour confirmer les paiements")
public class WebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final NotificationService notificationService;

    /**
     * Point d'entrée du webhook Stripe.
     *
     * ⚠️ Cet endpoint DOIT être exclu du filtre JWT (pas de token Bearer).
     * Stripe l'appelle depuis ses serveurs avec la signature dans l'en-tête Stripe-Signature.
     *
     * Pour tester en local : stripe listen --forward-to localhost:8000/api/v1/payments/webhook
     */
    @PostMapping("/webhook")
    @Operation(summary = "Webhook Stripe — Confirmation de paiement",
               description = "Reçoit les événements Stripe. Ne pas appeler manuellement.")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            // ✅ Vérification cryptographique de la signature Stripe — sécurité critique
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("🔴 Signature Stripe invalide — requête rejetée: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Signature invalide");
        }

        log.info("📩 Webhook Stripe reçu: type={}", event.getType());

        // On écoute uniquement la confirmation de session complétée
        if ("checkout.session.completed".equals(event.getType())) {
            try {
                com.stripe.model.EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                Session session;
                if (dataObjectDeserializer.getObject().isPresent()) {
                    session = (Session) dataObjectDeserializer.getObject().get();
                } else {
                    // Utilisation de deserializeUnsafe en cas de décalage de version d'API Stripe
                    session = (Session) dataObjectDeserializer.deserializeUnsafe();
                }

                // Récupération de l'ID du paiement depuis les Metadata
                String paymentIdStr = session.getMetadata().get("paymentId");
                if (paymentIdStr == null) {
                    log.error("🔴 Metadata 'paymentId' manquante dans la session Stripe: {}", session.getId());
                    return ResponseEntity.ok("Webhook reçu — metadata manquante");
                }

                Long paymentId = Long.valueOf(paymentIdStr);
                String paymentIntentId = session.getPaymentIntent();

                log.info("✅ Paiement confirmé par Stripe — paymentId={}, intentId={}", paymentId, paymentIntentId);

                // 1. Confirmer le paiement en base
                Payment payment = paymentService.confirmerPaiement(paymentId, paymentIntentId);

                // 2. Générer la quittance PDF et la récupérer en bytes pour l'email
                byte[] quittancePdf = null;
                try {
                    String receiptUrl = receiptService.generateReceipt(payment);
                    log.info("📄 Quittance générée: {}", receiptUrl);
                    quittancePdf = receiptService.loadReceipt(paymentId);
                } catch (Exception e) {
                    log.error("⚠️ Erreur génération quittance pour paiement {}: {}", paymentId, e.getMessage());
                }

                // 3. Notifier le locataire par email avec la quittance en pièce jointe
                // ✅ On extrait les champs primitifs avant d'appeler @Async pour éviter une LazyInitializationException
                String email = payment.getLocataire().getEmail();
                String prenom = payment.getLocataire().getFirstName();
                java.math.BigDecimal montant = payment.getMontantTotal();
                java.time.LocalDate moisConcerne = payment.getMoisConcerne();
                String refPaiement = payment.getReference();
                
                notificationService.notifierPaiementConfirme(email, prenom, montant, moisConcerne, refPaiement, quittancePdf);

            } catch (Exception e) {
                log.error("🔴 Erreur traitement webhook checkout.session.completed: {}", e.getMessage(), e);
                // On retourne 200 à Stripe malgré l'erreur interne pour éviter les re-tentatives répétées
                return ResponseEntity.ok("Webhook reçu — erreur interne loguée");
            }
        }

        return ResponseEntity.ok("Webhook reçu");
    }
}
