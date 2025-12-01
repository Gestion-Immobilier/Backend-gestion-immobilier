package univh2.fstm.gestionimmobilier.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.repository.PaymentRepository;

import java.io.File;
import java.io.FileOutputStream;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final PaymentRepository paymentRepository;

    private final String receiptFolder = "receipts/";

    // 1) GÉNÉRER UNE QUITTANCE PDF
    public String generateReceipt(Payment payment) throws Exception {

        File dir = new File(receiptFolder);
        if (!dir.exists()) dir.mkdirs();

        String filePath = receiptFolder + "receipt_" + payment.getId() + ".pdf";

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));

        document.open();
        document.add(new Paragraph("------------------------------------------"));
        document.add(new Paragraph("             QUITTANCE DE PAIEMENT"));
        document.add(new Paragraph("------------------------------------------"));
        document.add(new Paragraph("Quittance N° : " + payment.getId()));
        document.add(new Paragraph("Paiement N°  : " + payment.getId()));
        document.add(new Paragraph("Montant payé : " + payment.getAmount() + " MAD"));
        document.add(new Paragraph("Date         : " + payment.getCapturedAt()));
        document.add(new Paragraph("Propriété ID : " + payment.getPropertyId()));
        document.add(new Paragraph("Client ID    : " + payment.getUserId()));
        document.add(new Paragraph("\nSignature : SYSTEM"));
        document.add(new Paragraph("------------------------------------------"));

        document.close();

        return "/payments/receipt/" + payment.getId();
    }

    // 2) CHARGER LE PDF POUR TÉLÉCHARGEMENT
    public byte[] loadReceipt(Long paymentId) throws Exception {

        String filePath = receiptFolder + "receipt_" + paymentId + ".pdf";
        File file = new File(filePath);

        if (!file.exists()) {
            throw new Exception("Quittance introuvable !");
        }

        return java.nio.file.Files.readAllBytes(file.toPath());
    }
}
