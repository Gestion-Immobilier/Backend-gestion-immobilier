package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.request.PaymentInitRequest;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.model.PaymentStatus;
import univh2.fstm.gestionimmobilier.repository.PaymentRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 1) INITIALISER UN PAIEMENT
    public Payment initPayment(PaymentInitRequest request) {

        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setPropertyId(request.getPropertyId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCapturedAt(null);

        return paymentRepository.save(payment);
    }

    // 2) CAPTURER UN PAIEMENT
    public Payment capturePayment(Long paymentId) throws Exception {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("Paiement introuvable !"));

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            throw new Exception("Paiement déjà capturé.");
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCapturedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }
}
