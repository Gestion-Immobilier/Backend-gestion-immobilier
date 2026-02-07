package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.request.PaymentInitRequest;
import univh2.fstm.gestionimmobilier.model.*;
import univh2.fstm.gestionimmobilier.repository.ContratRepository;
import univh2.fstm.gestionimmobilier.repository.PaymentRepository;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContratRepository contratRepository;
    private final PersonneRepository personneRepository;

    // Initialiser un paiement basé sur un contrat
    public Payment initPayment(PaymentInitRequest request) throws Exception {
        // 1. Vérifier que le contrat existe
        Contrat contrat = contratRepository.findById(request.getContratId())
                .orElseThrow(() -> new Exception("Contrat introuvable !"));

        // 2. Vérifier que le contrat est actif
        if (contrat.getStatut() != StatutContrat.ACTIF) {
            throw new Exception("Le contrat n'est pas actif ! Statut: " + contrat.getStatut());
        }

        // 3. Vérifier que l'utilisateur est bien le locataire du contrat
        Personne locataire = personneRepository.findById(request.getUserId())
                .orElseThrow(() -> new Exception("Utilisateur introuvable !"));

        if (!contrat.getLocataire().getId().equals(locataire.getId())) {
            throw new Exception("Vous n'êtes pas le locataire de ce contrat !");
        }

        // 4. Vérifier que le locataire est bien de type LOCATAIRE
        if (locataire.getType() != Type.LOCATAIRE) {
            throw new Exception("L'utilisateur n'est pas un locataire !");
        }

        // 5. Déterminer le mois concerné
        LocalDate moisConcerne;
        if (request.getMoisConcerne() != null) {
            // S'assurer que c'est le premier jour du mois
            moisConcerne = request.getMoisConcerne().withDayOfMonth(1);
        } else {
            // Par défaut, le mois en cours
            moisConcerne = LocalDate.now().withDayOfMonth(1);
        }

        // 6. Vérifier que le contrat est valide pour ce mois
        LocalDate finMois = moisConcerne.withDayOfMonth(moisConcerne.lengthOfMonth());
        if (moisConcerne.isBefore(contrat.getDateDebut()) ||
                finMois.isAfter(contrat.getDateFin())) {
            throw new Exception("Le contrat n'est pas valide pour le mois " +
                    moisConcerne.getMonthValue() + "/" + moisConcerne.getYear());
        }

        // 7. Vérifier qu'il n'y a pas déjà un paiement CAPTURÉ pour ce mois
        Optional<Payment> paiementExistant = paymentRepository
                .findByContratAndMoisConcerne(contrat, moisConcerne);

        if (paiementExistant.isPresent()) {
            Payment existant = paiementExistant.get();
            if (existant.getStatus() == PaymentStatus.CAPTURED) {
                throw new Exception("Paiement déjà effectué pour ce mois !");
            } else if (existant.getStatus() == PaymentStatus.PENDING) {
                // Retourner le paiement existant en attente
                return existant;
            }
        }

        // 8. Calculer les montants depuis le contrat
        BigDecimal montantLoyer = contrat.getLoyerMensuel();
        BigDecimal montantCharges = contrat.getCharges() != null ?
                contrat.getCharges() : BigDecimal.ZERO;
        BigDecimal montantTotal = montantLoyer.add(montantCharges);

        // 9. Calculer la date d'échéance
        LocalDate dateEcheance;
        if (contrat.getJourPaiement() != null) {
            // Utiliser le jour de paiement spécifié dans le contrat
            dateEcheance = moisConcerne.withDayOfMonth(
                    Math.min(contrat.getJourPaiement(), moisConcerne.lengthOfMonth())
            );
        } else {
            // Par défaut, le 5 du mois suivant
            dateEcheance = moisConcerne.plusMonths(1).withDayOfMonth(5);
        }

        // 10. Créer le paiement
        Payment payment = Payment.builder()
                .contrat(contrat)
                .locataire(locataire)
                .montantLoyer(montantLoyer)
                .montantCharges(montantCharges)
                .montantTotal(montantTotal)
                .moisConcerne(moisConcerne)
                .dateEcheance(dateEcheance)
                .status(PaymentStatus.PENDING)
                .modePaiement(request.getPaymentMethod())
                .createdAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    // Capturer un paiement
    public Payment capturePayment(Long paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("Paiement introuvable !"));

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            throw new Exception("Paiement déjà capturé.");
        }

        // Vérifier que le contrat est toujours actif
        if (payment.getContrat().getStatut() != StatutContrat.ACTIF) {
            throw new Exception("Impossible de capturer le paiement : contrat non actif !");
        }

        // Vérifier que le mois n'a pas déjà été payé par un autre paiement
        boolean moisDejaPaye = paymentRepository.isMoisPaye(
                payment.getContrat().getId(),
                payment.getMoisConcerne()
        );

        if (moisDejaPaye) {
            throw new Exception("Ce mois a déjà été payé !");
        }

        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCapturedAt(LocalDateTime.now());

        // Générer une référence de transaction
        String ref = "TRX-" + System.currentTimeMillis();
        payment.setReferenceTransaction(ref);

        return paymentRepository.save(payment);
    }

    // Annuler un paiement
    public Payment cancelPayment(Long paymentId) throws Exception {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new Exception("Paiement introuvable !"));

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            // Vérifier si on peut annuler un paiement capturé
            // (délai de 24h par exemple)
            LocalDateTime limite = payment.getCapturedAt().plusHours(24);
            if (LocalDateTime.now().isAfter(limite)) {
                throw new Exception("Impossible d'annuler un paiement capturé depuis plus de 24h.");
            }
        }

        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }

    // Obtenir l'historique des paiements d'un contrat
    public List<Payment> getHistoriquePaiements(Long contratId) {
        return paymentRepository.findByContratIdOrderByMoisConcerneDesc(contratId);
    }

    // Obtenir les paiements d'un locataire
    public List<Payment> getPaiementsByLocataire(Long locataireId) {
        return paymentRepository.findByLocataireIdOrderByCreatedAtDesc(locataireId);
    }

    // Obtenir les paiements en attente
    public List<Payment> getPaiementsEnAttente() {
        return paymentRepository.findByStatus(PaymentStatus.PENDING);
    }

    // Obtenir les paiements en retard
    public List<Payment> getPaiementsEnRetard() {
        return paymentRepository.findPaiementsEnRetard();
    }

    // Générer les paiements mensuels automatiquement (pour cron job)
    public void genererPaiementsMensuels() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate premierDuMois = aujourdhui.withDayOfMonth(1);

        // Récupérer tous les contrats actifs
        List<Contrat> contratsActifs = contratRepository.findByStatut(StatutContrat.ACTIF);

        for (Contrat contrat : contratsActifs) {
            // Vérifier si le contrat est valide pour le mois en cours
            LocalDate finMois = premierDuMois.withDayOfMonth(premierDuMois.lengthOfMonth());

            if (!premierDuMois.isBefore(contrat.getDateDebut()) &&
                    !finMois.isAfter(contrat.getDateFin())) {

                // Vérifier si un paiement existe déjà pour ce mois
                boolean existeDeja = paymentRepository.isMoisPaye(contrat.getId(), premierDuMois);

                if (!existeDeja) {
                    try {
                        // Créer un paiement automatique
                        Payment payment = Payment.builder()
                                .contrat(contrat)
                                .locataire(contrat.getLocataire())
                                .montantLoyer(contrat.getLoyerMensuel())
                                .montantCharges(contrat.getCharges() != null ?
                                        contrat.getCharges() : BigDecimal.ZERO)
                                .montantTotal(contrat.getLoyerMensuel()
                                        .add(contrat.getCharges() != null ?
                                                contrat.getCharges() : BigDecimal.ZERO))
                                .moisConcerne(premierDuMois)
                                .dateEcheance(calculerDateEcheance(contrat, premierDuMois))
                                .status(PaymentStatus.PENDING)
                                .modePaiement("AUTOMATIQUE")
                                .createdAt(LocalDateTime.now())
                                .build();

                        paymentRepository.save(payment);
                    } catch (Exception e) {
                        // Logger l'erreur mais continuer avec les autres contrats
                        System.err.println("Erreur génération paiement contrat " +
                                contrat.getId() + ": " + e.getMessage());
                    }
                }
            }
        }
    }

    private LocalDate calculerDateEcheance(Contrat contrat, LocalDate moisConcerne) {
        if (contrat.getJourPaiement() != null) {
            return moisConcerne.withDayOfMonth(
                    Math.min(contrat.getJourPaiement(), moisConcerne.lengthOfMonth())
            );
        } else {
            return moisConcerne.plusMonths(1).withDayOfMonth(5);
        }
    }
}