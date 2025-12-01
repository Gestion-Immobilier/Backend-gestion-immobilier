package univh2.fstm.gestionimmobilier.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.PaymentRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

import java.io.File;
import java.io.FileOutputStream;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final PersonneRepository personneRepository;
    private final BienRepository bienRepository;
    private final String receiptFolder = "receipts/";

    public String generateReceipt(Payment payment) throws Exception {
        Personne locataire = personneRepository.findById(payment.getUserId())
                .orElseThrow(() -> new Exception("Locataire introuvable !"));

        File dir = new File(receiptFolder);
        if (!dir.exists()) dir.mkdirs();

        String filePath = receiptFolder + "receipt_" + payment.getId() + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();
        document.add(new Paragraph("--------------------------------------------------"));
        document.add(new Paragraph("             QUITTANCE DE PAIEMENT"));
        document.add(new Paragraph("--------------------------------------------------"));



        // Locataire
        document.add(new Paragraph("Locataire : " + locataire.getFirstName() + " " + locataire.getLastName()));
        document.add(new Paragraph("Adresse : " + locataire.getAdresse()));



        // Période et montant
        document.add(new Paragraph("Montant payé : " + payment.getAmount() + " MAD"));
        document.add(new Paragraph("Date du paiement : " + payment.getCapturedAt()));

        document.add(new Paragraph("--------------------------------------------------"));
        document.add(new Paragraph("Signature : SYSTEM"));

        document.close();

        return "/payments/receipt/" + payment.getId();
    }

    public byte[] loadReceipt(Long paymentId) throws Exception {
        String filePath = receiptFolder + "receipt_" + paymentId + ".pdf";
        File file = new File(filePath);

        if (!file.exists()) throw new Exception("Quittance introuvable !");

        return java.nio.file.Files.readAllBytes(file.toPath());
    }
}
