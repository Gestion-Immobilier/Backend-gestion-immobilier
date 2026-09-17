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
    public void notifierDemandeAcceptee(DemandeLocation demande) {
        String sujet = "🏠 Votre demande de location a été acceptée !";
        String contenu = String.format(
                "Bonjour %s,%n%n" +
                "Bonne nouvelle ! Votre demande de location pour le bien référencé '%s' a été acceptée.%n%n" +
                "Un contrat de location sera prochainement établi.%n%n" +
                "Cordialement,%n" +
                "L'équipe Gestion Immobilier",
                demande.getLocataire().getFirstName(),
                demande.getBien().getReference()
        );
        envoyerEmail(demande.getLocataire().getEmail(), sujet, contenu);
    }

    /**
     * Notifie le locataire que sa demande de location a été refusée.
     */
    @Async
    public void notifierDemandeRefusee(DemandeLocation demande) {
        String sujet = "❌ Votre demande de location a été refusée";
        String contenu = String.format(
                "Bonjour %s,%n%n" +
                "Nous vous informons que votre demande de location pour le bien référencé '%s' a été refusée.%n%n" +
                "Motif : %s%n%n" +
                "N'hésitez pas à consulter d'autres biens disponibles sur notre plateforme.%n%n" +
                "Cordialement,%n" +
                "L'équipe Gestion Immobilier",
                demande.getLocataire().getFirstName(),
                demande.getBien().getReference(),
                demande.getMotifRefus() != null ? demande.getMotifRefus() : "Non précisé"
        );
        envoyerEmail(demande.getLocataire().getEmail(), sujet, contenu);
    }

    /**
     * Notifie le locataire que son paiement a été confirmé,
     * avec la quittance PDF en pièce jointe.
     */
    @Async
    public void notifierPaiementConfirme(Payment payment, byte[] quittancePdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(payment.getLocataire().getEmail());
            helper.setSubject("✅ Confirmation de paiement — " + payment.getReference());
            helper.setText(String.format(
                    "Bonjour %s,%n%n" +
                    "Votre paiement de %.2f MAD pour le mois %s/%s a bien été reçu et confirmé.%n%n" +
                    "Vous trouverez votre quittance de loyer en pièce jointe.%n%n" +
                    "Référence : %s%n%n" +
                    "Cordialement,%n" +
                    "L'équipe Gestion Immobilier",
                    payment.getLocataire().getFirstName(),
                    payment.getMontantTotal(),
                    payment.getMoisConcerne().getMonthValue(),
                    payment.getMoisConcerne().getYear(),
                    payment.getReference()
            ));

            if (quittancePdf != null && quittancePdf.length > 0) {
                helper.addAttachment(
                        "quittance_" + payment.getReference() + ".pdf",
                        new ByteArrayResource(quittancePdf)
                );
            }

            mailSender.send(message);
            log.info("📧 Email de confirmation de paiement envoyé à: {}", payment.getLocataire().getEmail());

        } catch (MessagingException e) {
            // IMPORTANT : Ne jamais faire échouer la transaction métier à cause d'un email raté
            log.error("❌ Échec envoi email de confirmation paiement à {}: {}",
                    payment.getLocataire().getEmail(), e.getMessage());
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
