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


@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ReceiptService receiptService;

    //-------------------------------------
    // 1) INITIALISER UN PAIEMENT (PENDING)
    //-------------------------------------
    @PostMapping("/init")
    public ResponseEntity<?> initPayment(@RequestBody PaymentInitRequest request) {

        Payment payment = paymentService.initPayment(request);
        
        return ResponseEntity.ok(
                new Object() {
                    public final Long paymentId = payment.getId();
                    public final String status = payment.getStatus().name();
                    public final double amount = payment.getAmount();
                    public final String currency = "MAD";
                    public final String message = "Paiement initialisé avec succès.";
                }
        );
    }

    //-------------------------------------
    // 2) CAPTURER / VALIDER LE PAIEMENT
    //-------------------------------------
    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<?> capturePayment(@PathVariable Long paymentId) throws Exception {

        Payment payment = paymentService.capturePayment(paymentId);

        // Génération PDF
        String receiptUrl = receiptService.generateReceipt(payment);

        return ResponseEntity.ok(
                new Object() {
                    public final Long paymentId = payment.getId();
                    public final String status = payment.getStatus().name();
                    public final String capturedAt = payment.getCapturedAt().toString();
//                    public final String receiptUrlStr = receiptUrl;
                    public final String message = "Paiement capturé et quittance générée.";
                }
        );
    }

    //-------------------------------------
    // 3) TÉLÉCHARGER LA QUITTANCE PDF
    //-------------------------------------
    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long paymentId) throws Exception {

        byte[] pdf = receiptService.loadReceipt(paymentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_" + paymentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
