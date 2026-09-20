package univh2.fstm.gestionimmobilier.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.DemandeLocation;
import univh2.fstm.gestionimmobilier.model.Payment;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    /**
     * Notifie le locataire que sa demande de location a été acceptée.
     */
    @Async
    public void notifierDemandeAcceptee(String emailLocataire, String prenomLocataire, String referenceBien) {
        String sujet = "🏠 Votre demande de location a été acceptée !";
        String contenu = String.format(
                "Bonjour %s,%n%n" +
                "Bonne nouvelle ! Votre demande de location pour le bien référencé '%s' a été acceptée.%n%n" +
                "Un contrat de location sera prochainement établi.%n%n" +
                "Cordialement,%n" +
                "L'équipe Gestion Immobilier",
                prenomLocataire,
                referenceBien
        );
        envoyerEmail(emailLocataire, sujet, contenu);
    }

    /**
     * Notifie le locataire que sa demande de location a été refusée.
     */
    @Async
    public void notifierDemandeRefusee(String emailLocataire, String prenomLocataire, String referenceBien, String motifRefus) {
        String sujet = "❌ Votre demande de location a été refusée";
        String contenu = String.format(
                "Bonjour %s,%n%n" +
                "Nous vous informons que votre demande de location pour le bien référencé '%s' a été refusée.%n%n" +
                "Motif : %s%n%n" +
                "N'hésitez pas à consulter d'autres biens disponibles sur notre plateforme.%n%n" +
                "Cordialement,%n" +
                "L'équipe Gestion Immobilier",
                prenomLocataire,
                referenceBien,
                motifRefus != null ? motifRefus : "Non précisé"
        );
        envoyerEmail(emailLocataire, sujet, contenu);
    }

    /**
     * Notifie le locataire que son paiement a été confirmé,
     * avec la quittance PDF en pièce jointe.
     */
    @Async
    public void notifierPaiementConfirme(String emailLocataire, String prenomLocataire,
                                         java.math.BigDecimal montantTotal, java.time.LocalDate moisConcerne,
                                         String referencePaiement, byte[] quittancePdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(emailLocataire);
            helper.setSubject("✅ Confirmation de paiement — " + referencePaiement);
            helper.setText(String.format(
                    "Bonjour %s,%n%n" +
                    "Votre paiement de %.2f MAD pour le mois %s/%s a bien été reçu et confirmé.%n%n" +
                    "Vous trouverez votre quittance de loyer en pièce jointe.%n%n" +
                    "Référence : %s%n%n" +
                    "Cordialement,%n" +
                    "L'équipe Gestion Immobilier",
                    prenomLocataire,
                    montantTotal,
                    moisConcerne.getMonthValue(),
                    moisConcerne.getYear(),
                    referencePaiement
            ));

            if (quittancePdf != null && quittancePdf.length > 0) {
                helper.addAttachment(
                        "quittance_" + referencePaiement + ".pdf",
                        new ByteArrayResource(quittancePdf)
                );
            }

            mailSender.send(message);
            log.info("📧 Email de confirmation de paiement envoyé à: {}", emailLocataire);

        } catch (MessagingException e) {
            log.error("❌ Échec envoi email de confirmation paiement à {}: {}",
                    emailLocataire, e.getMessage());
        }
    }

    /**
     * Méthode utilitaire d'envoi d'email simple.
     * Le try/catch garantit qu'un email raté ne brise jamais le flux principal.
     */
    private void envoyerEmail(String destinataire, String sujet, String contenu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(contenu);
            mailSender.send(message);
            log.info("📧 Email envoyé à: {}", destinataire);
        } catch (Exception e) {
            log.error("❌ Échec envoi email à {}: {}", destinataire, e.getMessage());
        }
    }
}
