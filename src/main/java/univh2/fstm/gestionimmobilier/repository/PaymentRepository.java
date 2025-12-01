package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univh2.fstm.gestionimmobilier.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
