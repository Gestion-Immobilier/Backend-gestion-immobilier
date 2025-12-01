package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.request.PaymentInitRequest;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.model.PaymentStatus;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.PaymentRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

import java.time.LocalDateTime;



@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PersonneRepository personneRepository;

    // Initialiser un paiement
    public Payment initPayment(PaymentInitRequest request) throws Exception {
        // Vérifier que le locataire existe
        Personne locataire = personneRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("Locataire introuvable !"));
        if (locataire.getType() != Type.LOCATAIRE) {
            throw new Exception("L'utilisateur n'est pas un locataire !");
        }

        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCapturedAt(null);

        return paymentRepository.save(payment);
    }

    // Capturer un paiement
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