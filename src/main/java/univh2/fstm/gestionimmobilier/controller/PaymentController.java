package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.request.PaymentInitRequest;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.service.impl.PaymentReceiptRequest;
import univh2.fstm.gestionimmobilier.service.impl.PaymentService;
import univh2.fstm.gestionimmobilier.service.impl.ReceiptService;


@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ReceiptService receiptService;

    @PostMapping("/init")
    public ResponseEntity<?> initPayment(@RequestBody PaymentInitRequest request) throws Exception {
        Payment payment = paymentService.initPayment(request);
        return ResponseEntity.ok(new Object() {
            public final Long paymentId = payment.getId();
            public final String status = payment.getStatus().name();
            public final double amount = payment.getAmount();
            public final String currency = "MAD";
            public final String message = "Paiement initialisé avec succès.";
        });
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<?> capturePayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.capturePayment(paymentId);
            String receiptUrl = receiptService.generateReceipt(payment);

            return ResponseEntity.ok(new Object() {
                public final Long paymentId = payment.getId();
                public final String status = payment.getStatus().name();
                public final String capturedAt = payment.getCapturedAt().toString();
                public final String receiptUrlStr = receiptUrl;
                public final String message = "Paiement capturé et quittance générée.";
            });

        } catch (Exception e) {
            e.printStackTrace(); // pour voir l'erreur exacte dans la console
            return ResponseEntity.status(500).body(new Object() {
                public final String error = e.getMessage();
            });
        }
    }


    @GetMapping("/receipt/{paymentId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long paymentId) throws Exception {
        byte[] pdf = receiptService.loadReceipt(paymentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_" + paymentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
