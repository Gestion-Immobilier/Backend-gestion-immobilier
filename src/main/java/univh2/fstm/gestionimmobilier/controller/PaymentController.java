package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.request.PaymentInitRequest;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.service.impl.PaymentService;
import univh2.fstm.gestionimmobilier.service.impl.ReceiptService;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ReceiptService receiptService;

    @PostMapping("/init")
    public ResponseEntity<?> initPayment(@RequestBody PaymentInitRequest request) {
        try {
            Payment payment = paymentService.initPayment(request);
            return ResponseEntity.ok(new Object() {
                public final Long id = payment.getId();
                public final String reference = payment.getReference();
                public final String status = payment.getStatus().name();
                public final String moisConcerne = payment.getMoisConcerne().toString();
                public final Double montantTotal = payment.getMontantTotal().doubleValue();
                public final String dateEcheance = payment.getDateEcheance().toString();
                public final String message = "Paiement initialisé avec succès";
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = e.getMessage();
            });
        }
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<?> capturePayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.capturePayment(paymentId);
            String receiptUrl = receiptService.generateReceipt(payment);

            return ResponseEntity.ok(new Object() {
                public final Long id = payment.getId();
                public final String reference = payment.getReference();
                public final String status = payment.getStatus().name();
                public final String capturedAt = payment.getCapturedAt().toString();
                public final String receiptUrlStr = receiptUrl; // Changé de receiptUrl à receiptUrlStr
                public final String message = "Paiement capturé et quittance générée";
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = e.getMessage();
            });
        }
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(new Object() {
                public final Long id = payment.getId();
                public final String reference = payment.getReference();
                public final String status = payment.getStatus().name();
                public final String message = "Paiement annulé";
            });
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Object() {
                public final String error = e.getMessage();
            });
        }
    }

    @GetMapping("/contrat/{contratId}")
    public ResponseEntity<?> getPaiementsByContrat(@PathVariable Long contratId) {
        List<Payment> paiements = paymentService.getHistoriquePaiements(contratId);
        return ResponseEntity.ok(paiements);
    }

    @GetMapping("/locataire/{locataireId}")
    public ResponseEntity<?> getPaiementsByLocataire(@PathVariable Long locataireId) {
        List<Payment> paiements = paymentService.getPaiementsByLocataire(locataireId);
        return ResponseEntity.ok(paiements);
    }

    @GetMapping("/en-attente")
    public ResponseEntity<?> getPaiementsEnAttente() {
        List<Payment> paiements = paymentService.getPaiementsEnAttente();
        return ResponseEntity.ok(paiements);
    }

    @GetMapping("/en-retard")
    public ResponseEntity<?> getPaiementsEnRetard() {
        List<Payment> paiements = paymentService.getPaiementsEnRetard();
        return ResponseEntity.ok(paiements);
    }

    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long paymentId) throws Exception {
        byte[] pdf = receiptService.loadReceipt(paymentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quittance_" + paymentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}