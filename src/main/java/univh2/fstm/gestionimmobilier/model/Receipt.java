package univh2.fstm.gestionimmobilier.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long paymentId;

    private LocalDateTime issuedAt = LocalDateTime.now();

    private String pdfUrl; // path MinIO, local, etc.
}
