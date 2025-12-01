package univh2.fstm.gestionimmobilier.service.impl;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PaymentReceiptRequest {

    // Bailleur
    private String bailleurNom;
    private String bailleurAdresse;
    private String bailleurTelephone;

    // Locataire
    private String locataireNom;
    private String locataireAdresse;
    private String locataireTelephone;

    // Bien loué
    private String bienNom;
    private String bienAdresse;

    // Période concernée
    private LocalDate periodeDebut;
    private LocalDate periodeFin;

    // Paiement
    private double montantLoyer;
    private double montantCharges;
    private double montantTotal;
    private LocalDate datePaiement;
    private String modePaiement;

    // Infos administratives
    private String lieuEtablissement;
    private LocalDate dateEtablissement;
}
