package univh2.fstm.gestionimmobilier.model;

public enum StatutDemandeResiliation {
    EN_ATTENTE,          // Demande soumise, en attente de traitement
    EN_COURS_EXAMEN,     // En cours d'examen par l'admin
    ACCEPTEE,            // Acceptée par l'admin
    REFUSEE,             // Refusée par l'admin
    ANNULEE              // Annulée par le locataire
}
