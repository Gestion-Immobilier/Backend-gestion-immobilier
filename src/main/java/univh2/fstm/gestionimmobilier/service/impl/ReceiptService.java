package univh2.fstm.gestionimmobilier.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Bien;
import univh2.fstm.gestionimmobilier.model.Contrat;
import univh2.fstm.gestionimmobilier.model.Payment;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.repository.BienRepository;
import univh2.fstm.gestionimmobilier.repository.PaymentRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// Import java.awt.Color au lieu de BaseColor
import java.awt.Color;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final PersonneRepository personneRepository;
    private final BienRepository bienRepository;
    private final PaymentRepository paymentRepository;
    private final String receiptFolder = "receipts/";

    // Utiliser java.awt.Color au lieu de BaseColor
    private static final Color HEADER_COLOR = new Color(41, 128, 185); // Bleu professionnel
    private static final Color ACCENT_COLOR = new Color(52, 152, 219); // Bleu clair
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color DARK_GRAY = new Color(51, 51, 51);

    public String generateReceipt(Payment payment) throws Exception {
        // Récupérer le contrat associé
        Contrat contrat = payment.getContrat();

        // Récupérer les informations depuis le contrat
        Personne locataire = contrat.getLocataire();
        Personne proprietaire = contrat.getBien().getProprietaire();
        Bien bien = contrat.getBien();

        File dir = new File(receiptFolder);
        if (!dir.exists()) dir.mkdirs();

        String filePath = receiptFolder + "quittance_" + payment.getId() + ".pdf";

        Document document = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        writer.setPageEvent(new HeaderFooter());

        document.open();

        // Police personnalisée
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, HEADER_COLOR);
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, DARK_GRAY);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, DARK_GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, DARK_GRAY);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, HEADER_COLOR);
        Font receiptNumberFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.WHITE);

        // === EN-TÊTE AVEC NUMÉRO DE QUITTANCE ===
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{70, 30});

        PdfPCell titleCell = new PdfPCell(new Phrase("QUITTANCE DE LOYER", titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setPaddingBottom(10);
        headerTable.addCell(titleCell);

        PdfPCell numberCell = new PdfPCell(new Phrase("N° " + payment.getReference(), receiptNumberFont));
        numberCell.setBackgroundColor(HEADER_COLOR);
        numberCell.setBorder(Rectangle.NO_BORDER);
        numberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        numberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numberCell.setPadding(10);
        headerTable.addCell(numberCell);

        document.add(headerTable);

        document.add(createSeparatorLine());
        document.add(Chunk.NEWLINE);

        // === INFORMATIONS DE BASE ===
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateEtablie = LocalDate.now().format(formatter);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{50, 50});
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(20);

        infoTable.addCell(createInfoCell("BAILLEUR", proprietaire));
        infoTable.addCell(createInfoCell("LOCATAIRE", locataire));

        document.add(infoTable);

        // === DÉTAILS DE LA TRANSACTION ===
        Paragraph detailsTitle = new Paragraph("DÉTAILS DE LA TRANSACTION", sectionFont);
        detailsTitle.setSpacingBefore(20);
        detailsTitle.setSpacingAfter(15);
        document.add(detailsTitle);

        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setWidths(new float[]{40, 60});
        detailsTable.setSpacingBefore(10);
        detailsTable.setSpacingAfter(20);

        // Référence du contrat
        addDetailRow(detailsTable, "Référence contrat", contrat.getReference(), labelFont, valueFont);

        // Référence du bien
        addDetailRow(detailsTable, "Référence bien", bien.getReference(), labelFont, valueFont);

        // Date d'émission
        addDetailRow(detailsTable, "Date d'émission", dateEtablie, labelFont, valueFont);

        // Date de paiement
        if (payment.getCapturedAt() != null) {
            String datePaiement = payment.getCapturedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm"));
            addDetailRow(detailsTable, "Date de paiement", datePaiement, labelFont, valueFont);
        }

        // Adresse du bien
        addDetailRow(detailsTable, "Adresse du bien",
                bien.getAdresse() + ", " + bien.getCodePostal() + " " + bien.getVille(),
                labelFont, normalFont);

        // Type de bien
        if (bien.getTypeBien() != null) {
            addDetailRow(detailsTable, "Type de bien", bien.getTypeBien().toString(), labelFont, valueFont);
        }

        document.add(detailsTable);

        // === PÉRIODE DE PAIEMENT ===
        LocalDate moisConcerne = payment.getMoisConcerne();
        LocalDate finMois = moisConcerne.withDayOfMonth(moisConcerne.lengthOfMonth());

        Paragraph periodeText = new Paragraph(
                "Période de paiement : " +
                        moisConcerne.format(formatter) + " au " +
                        finMois.format(formatter),
                labelFont
        );
        periodeText.setSpacingBefore(10);
        periodeText.setSpacingAfter(20);
        document.add(periodeText);

        // === TABLEAU DES MONTANTS ===
        Paragraph montantsTitle = new Paragraph("RÉCAPITULATIF DES MONTANTS", sectionFont);
        montantsTitle.setSpacingBefore(20);
        montantsTitle.setSpacingAfter(15);
        document.add(montantsTitle);

        float[] columnWidths = {40, 30, 30};
        PdfPTable montantsTable = new PdfPTable(columnWidths);
        montantsTable.setWidthPercentage(100);
        montantsTable.setSpacingBefore(10);
        montantsTable.setSpacingAfter(30);

        // En-têtes du tableau
        String[] headers = {"DESCRIPTION", "MONTANT", "SOUS-TOTAL"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, labelFont));
            cell.setBackgroundColor(LIGHT_GRAY);
            cell.setBorderWidth(1);
            cell.setBorderColor(Color.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(10);
            montantsTable.addCell(cell);
        }

        // Loyer nu
        addMontantRow(montantsTable, "Loyer mensuel",
                payment.getMontantLoyer() + " MAD",
                payment.getMontantLoyer() + " MAD");

        // Charges
        if (payment.getMontantCharges().compareTo(java.math.BigDecimal.ZERO) > 0) {
            addMontantRow(montantsTable, "Charges",
                    payment.getMontantCharges() + " MAD",
                    payment.getMontantCharges() + " MAD");
        }

        // Ligne vide avant le total
        PdfPCell emptyCell = new PdfPCell(new Phrase(""));
        emptyCell.setColspan(3);
        emptyCell.setBorder(Rectangle.NO_BORDER);
        emptyCell.setPadding(5);
        montantsTable.addCell(emptyCell);

        // Ligne du TOTAL
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL PAYÉ", labelFont));
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPadding(10);
        montantsTable.addCell(totalLabelCell);

        PdfPCell emptyCell2 = new PdfPCell(new Phrase(""));
        emptyCell2.setBorder(Rectangle.NO_BORDER);
        emptyCell2.setPadding(10);
        montantsTable.addCell(emptyCell2);

        PdfPCell totalCell = new PdfPCell(new Phrase(payment.getMontantTotal() + " MAD", amountFont));
        totalCell.setBorder(Rectangle.NO_BORDER);
        totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalCell.setPadding(10);
        montantsTable.addCell(totalCell);

        document.add(montantsTable);

        // === INFORMATIONS SUPPLEMENTAIRES ===
        Paragraph infoContrat = new Paragraph("Informations du contrat:", labelFont);
        infoContrat.setSpacingBefore(20);
        document.add(infoContrat);

        PdfPTable infoContratTable = new PdfPTable(2);
        infoContratTable.setWidthPercentage(100);
        infoContratTable.setWidths(new float[]{40, 60});
        infoContratTable.setSpacingBefore(10);

        addDetailRow(infoContratTable, "Date début contrat",
                contrat.getDateDebut().format(formatter), smallFont, normalFont);
        addDetailRow(infoContratTable, "Date fin contrat",
                contrat.getDateFin().format(formatter), smallFont, normalFont);
        addDetailRow(infoContratTable, "Durée",
                contrat.getDureeContrat() + " mois", smallFont, normalFont);
        addDetailRow(infoContratTable, "Type contrat",
                contrat.getTypeContrat().toString(), smallFont, normalFont);

        document.add(infoContratTable);

        // === SIGNATURE ===
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{50, 50});
        signatureTable.setSpacingBefore(40);

        // Signature du bailleur
        PdfPCell signatureCell = new PdfPCell();
        signatureCell.setBorder(Rectangle.NO_BORDER);

        Paragraph signatureTitle = new Paragraph("Pour le bailleur", labelFont);
        signatureTitle.setSpacingAfter(20);
        signatureCell.addElement(signatureTitle);

        Paragraph signatureLine = new Paragraph("_________________________", normalFont);
        signatureLine.setSpacingAfter(5);
        signatureCell.addElement(signatureLine);

        if (proprietaire != null) {
            Paragraph nomProprietaire = new Paragraph(
                    proprietaire.getFirstName() + " " + proprietaire.getLastName(),
                    normalFont
            );
            signatureCell.addElement(nomProprietaire);
        }

        Paragraph dateSignature = new Paragraph(
                "Fait à " + bien.getVille() + ", le " + dateEtablie,
                smallFont
        );
        signatureCell.addElement(dateSignature);

        signatureTable.addCell(signatureCell);

        // Cachet (optionnel)
        PdfPCell cachetCell = new PdfPCell();
        cachetCell.setBorder(Rectangle.NO_BORDER);
        cachetCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph cachetText = new Paragraph("CACHET / TIMBRE", smallFont);
        cachetText.setAlignment(Element.ALIGN_CENTER);
        cachetCell.addElement(cachetText);

        // Rectangle pour cachet
        PdfPTable cachetTable = new PdfPTable(1);
        cachetTable.setTotalWidth(100);
        cachetTable.setLockedWidth(true);

        PdfPCell cachetRect = new PdfPCell(new Phrase(""));
        cachetRect.setFixedHeight(80);
        cachetRect.setBorder(Rectangle.RECTANGLE);
        cachetRect.setBorderWidth(1);
        cachetRect.setBorderColor(Color.LIGHT_GRAY);
        cachetRect.setHorizontalAlignment(Element.ALIGN_CENTER);
        cachetRect.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cachetTable.addCell(cachetRect);

        cachetCell.addElement(cachetTable);
        signatureTable.addCell(cachetCell);

        document.add(signatureTable);

        // === MENTION LÉGALE ===
        Paragraph mention = new Paragraph(
                "Cette quittance annule tous les reçus qui auraient pu être antérieurement donnés " +
                        "pour acompte sur le présent loyer et fait foi de paiement entre les parties.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY)
        );
        mention.setAlignment(Element.ALIGN_CENTER);
        mention.setSpacingBefore(30);
        document.add(mention);

        document.close();

        return "/payments/receipt/" + payment.getId();
    }

    // === MÉTHODES UTILITAIRES ===

    private PdfPCell createInfoCell(String title, Personne personne) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(1);
        cell.setBorderColor(LIGHT_GRAY);
        cell.setBackgroundColor(new Color(250, 250, 250));
        cell.setPadding(15);
        cell.setBorderWidthTop(0);
        cell.setBorderWidthLeft(0);
        cell.setBorderWidthRight(0);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, HEADER_COLOR);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, DARK_GRAY);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);

        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setSpacingAfter(10);
        cell.addElement(titlePara);

        if (personne != null) {
            cell.addElement(new Paragraph(
                    personne.getFirstName() + " " + personne.getLastName(),
                    normalFont
            ));

            if (personne.getAdresse() != null) {
                cell.addElement(new Paragraph(personne.getAdresse(), normalFont));
            }

            if (personne.getPhone() != null) {
                cell.addElement(new Paragraph("Tél: " + personne.getPhone(), normalFont));
            }

            if (personne.getEmail() != null) {
                cell.addElement(new Paragraph(personne.getEmail(), normalFont));
            }
        } else {
            cell.addElement(new Paragraph("Non spécifié", smallFont));
        }

        return cell;
    }

    private void addDetailRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + " :", labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        labelCell.setPaddingLeft(0);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        valueCell.setPaddingRight(0);
        table.addCell(valueCell);
    }

    private void addMontantRow(PdfPTable table, String description, String montant, String sousTotal) {
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, DARK_GRAY);

        PdfPCell descCell = new PdfPCell(new Phrase(description, normalFont));
        descCell.setBorderWidth(1);
        descCell.setBorderColor(Color.LIGHT_GRAY);
        descCell.setPadding(10);
        table.addCell(descCell);

        PdfPCell montantCell = new PdfPCell(new Phrase(montant, normalFont));
        montantCell.setBorderWidth(1);
        montantCell.setBorderColor(Color.LIGHT_GRAY);
        montantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        montantCell.setPadding(10);
        table.addCell(montantCell);

        PdfPCell sousTotalCell = new PdfPCell(new Phrase(sousTotal, normalFont));
        sousTotalCell.setBorderWidth(1);
        sousTotalCell.setBorderColor(Color.LIGHT_GRAY);
        sousTotalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        sousTotalCell.setPadding(10);
        table.addCell(sousTotalCell);
    }

    private Paragraph createSeparatorLine() {
        Paragraph line = new Paragraph();
        line.add(new Chunk(
                "_______________________________________________________________________________",
                FontFactory.getFont(FontFactory.HELVETICA, 1, ACCENT_COLOR)
        ));
        return line;
    }

    // Classe interne pour en-tête et pied de page
    class HeaderFooter extends PdfPageEventHelper {
        private Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(500);
            footer.setLockedWidth(true);
            footer.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPCell cell = new PdfPCell(new Phrase(
                    "Gestion Immobilier - Système de gestion des loyers - Page " +
                            writer.getPageNumber(),
                    footerFont
            ));
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            footer.addCell(cell);

            footer.writeSelectedRows(0, -1,
                    (document.right() - document.left()) / 2 + document.leftMargin() - 250,
                    document.bottom() - 20,
                    writer.getDirectContent()
            );
        }
    }

    public byte[] loadReceipt(Long paymentId) throws Exception {
        String filePath = receiptFolder + "quittance_" + paymentId + ".pdf";
        File file = new File(filePath);

        if (!file.exists()) {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new Exception("Paiement introuvable !"));
            generateReceipt(payment);
            file = new File(filePath);
        }

        return java.nio.file.Files.readAllBytes(file.toPath());
    }
}