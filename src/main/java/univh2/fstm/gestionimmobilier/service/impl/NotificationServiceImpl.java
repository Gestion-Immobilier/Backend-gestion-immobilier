//// NotificationServiceImpl.java
//package univh2.fstm.gestionimmobilier.service.impl;
//
//import univh2.fstm.gestionimmobilier.dto.request.DemandeNotificationRequestDto;
//import univh2.fstm.gestionimmobilier.dto.response.DemandeNotificationResponseDto;
//import univh2.fstm.gestionimmobilier.exception.ResourceNotFoundException;
//import univh2.fstm.gestionimmobilier.model.*;
//import univh2.fstm.gestionimmobilier.repository.*;
//import univh2.fstm.gestionimmobilier.service.interfaces.NotificationService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationServiceImpl implements NotificationService {
//
//    private final NotificationRepository notificationRepository;
//    private final PersonneRepository userRepository;
//    private final DemandeLocationRepository demandeLocationRepository;
//    private final PaymentRepository paiementRepository;
////    private final ReclamationRepository reclamationRepository;
//    private final ContratRepository contratRepository;
//    private final VisiteRepository visiteRepository;
//    private final ModelMapper modelMapper;
//
//    @Override
//    @Transactional
//    public DemandeNotificationResponseDto creerNotification(DemandeNotificationRequestDto requestDto) {
//        log.info("Création d'une notification pour l'utilisateur {}", requestDto.getUtilisateurId());
//
//        // Validation utilisateur
//        User utilisateur = userRepository.findById(requestDto.getUtilisateurId())
//                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
//
//        // Créer la notification
//        Notification notification = Notification.builder()
//                .utilisateur(utilisateur)
//                .titre(requestDto.getTitre())
//                .message(requestDto.getMessage())
//                .type(requestDto.getType())
//                .lue(false)
//                .referenceId(requestDto.getReferenceId())
//                .referenceType(requestDto.getReferenceType())
//                .metadata(requestDto.getMetadata() != null ? requestDto.getMetadata() : new HashMap<>())
//                .dateEnvoi(LocalDateTime.now())
//                .build();
//
//        notification = notificationRepository.save(notification);
//
//        log.info("Notification créée avec ID: {}", notification.getId());
//
//        return mapToDto(notification);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public DemandeNotificationResponseDto getNotificationById(Long id) {
//        Notification notification = notificationRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec ID: " + id));
//
//        return mapToDto(notification);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<DemandeNotificationResponseDto> getNotificationsByUser(Long utilisateurId) {
//        // Vérifier que l'utilisateur existe
//        if (!userRepository.existsById(utilisateurId)) {
//            throw new ResourceNotFoundException("Utilisateur non trouvé avec ID: " + utilisateurId);
//        }
//
//        List<Notification> notifications = notificationRepository
//                .findByUtilisateurIdOrderByDateEnvoiDesc(utilisateurId);
//
//        return notifications.stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<DemandeNotificationResponseDto> getNotificationsNonLues(Long utilisateurId) {
//        if (!userRepository.existsById(utilisateurId)) {
//            throw new ResourceNotFoundException("Utilisateur non trouvé avec ID: " + utilisateurId);
//        }
//
//        List<Notification> notifications = notificationRepository
//                .findByUtilisateurIdAndLueFalseOrderByDateEnvoiDesc(utilisateurId);
//
//        return notifications.stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public long countNotificationsNonLues(Long utilisateurId) {
//        if (!userRepository.existsById(utilisateurId)) {
//            return 0;
//        }
//
//        return notificationRepository.countByUtilisateurIdAndLueFalse(utilisateurId);
//    }
//
//    @Override
//    @Transactional
//    public DemandeNotificationResponseDto marquerCommeLue(Long id) {
//        Notification notification = notificationRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Notification non trouvée avec ID: " + id));
//
//        if (!notification.isLue()) {
//            notification.setLue(true);
//            notification.setDateLecture(LocalDateTime.now());
//            notification = notificationRepository.save(notification);
//
//            log.info("Notification {} marquée comme lue", id);
//        }
//
//        return mapToDto(notification);
//    }
//
//    @Override
//    @Transactional
//    public void marquerToutCommeLu(Long utilisateurId) {
//        if (!userRepository.existsById(utilisateurId)) {
//            throw new ResourceNotFoundException("Utilisateur non trouvé avec ID: " + utilisateurId);
//        }
//
//        notificationRepository.marquerToutCommeLu(utilisateurId, LocalDateTime.now());
//        log.info("Toutes les notifications de l'utilisateur {} marquées comme lues", utilisateurId);
//    }
//
//    @Override
//    @Transactional
//    public void deleteNotification(Long id) {
//        if (!notificationRepository.existsById(id)) {
//            throw new ResourceNotFoundException("Notification non trouvée avec ID: " + id);
//        }
//
//        notificationRepository.deleteById(id);
//        log.info("Notification {} supprimée", id);
//    }
//
//    @Override
//    @Transactional
//    public void deleteAllByUser(Long utilisateurId) {
//        if (!userRepository.existsById(utilisateurId)) {
//            throw new ResourceNotFoundException("Utilisateur non trouvé avec ID: " + utilisateurId);
//        }
//
//        notificationRepository.deleteByUtilisateurId(utilisateurId);
//        log.info("Toutes les notifications de l'utilisateur {} supprimées", utilisateurId);
//    }
//
//    // =========================================================================
//    // MÉTHODES UTILITAIRES POUR CRÉER DES NOTIFICATIONS SPÉCIFIQUES
//    // =========================================================================
//
//    @Override
//    @Transactional
//    public void creerNotificationDemandeLocation(Long demandeId, Long locataireId) {
//        DemandeLocation demande = demandeLocationRepository.findById(demandeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("bienId", demande.getBien().getId());
//        metadata.put("bienReference", demande.getBien().getReference());
//        metadata.put("dateDebut", demande.getDateDebut().toString());
//        metadata.put("dureeContrat", demande.getDureeContrat());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Demande de location envoyée")
//                .message("Votre demande pour le bien " + demande.getBien().getReference() + " a été envoyée avec succès.")
//                .type("LOCATION_DEMANDE")
//                .referenceId(demandeId)
//                .referenceType("DEMANDE_LOCATION")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationDemandeAcceptee(Long demandeId, Long locataireId) {
//        DemandeLocation demande = demandeLocationRepository.findById(demandeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("bienId", demande.getBien().getId());
//        metadata.put("bienReference", demande.getBien().getReference());
//        metadata.put("loyerMensuel", demande.getBien().getLoyerMensuel());
//        metadata.put("dateDebut", demande.getDateDebut().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Demande de location acceptée")
//                .message("Félicitations ! Votre demande pour le bien " +
//                        demande.getBien().getReference() + " a été acceptée.")
//                .type("LOCATION_RESPONSE_ACCEPTED")
//                .referenceId(demandeId)
//                .referenceType("DEMANDE_LOCATION")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationDemandeRefusee(Long demandeId, Long locataireId, String motif) {
//        DemandeLocation demande = demandeLocationRepository.findById(demandeId)
//                .orElseThrow(() -> new ResourceNotFoundException("Demande non trouvée"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("bienId", demande.getBien().getId());
//        metadata.put("bienReference", demande.getBien().getReference());
//        metadata.put("motifRefus", motif);
//        metadata.put("dateTraitement", LocalDateTime.now().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Demande de location refusée")
//                .message("Votre demande pour le bien " + demande.getBien().getReference() +
//                        " a été refusée. Motif: " + motif)
//                .type("LOCATION_RESPONSE_REFUSED")
//                .referenceId(demandeId)
//                .referenceType("DEMANDE_LOCATION")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationPaiementCree(Long paiementId, Long locataireId) {
//        Paiement paiement = paiementRepository.findById(paiementId)
//                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("montant", paiement.getMontant());
//        metadata.put("mois", paiement.getMois());
//        metadata.put("annee", paiement.getAnnee());
//        metadata.put("dateEcheance", paiement.getDateEcheance().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Nouveau paiement créé")
//                .message("Un paiement de " + paiement.getMontant() + " DH pour " +
//                        paiement.getMois() + " " + paiement.getAnnee() + " a été créé.")
//                .type("PAYMENT_CREATED")
//                .referenceId(paiementId)
//                .referenceType("PAIEMENT")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationPaiementConfirme(Long paiementId, Long locataireId) {
//        Paiement paiement = paiementRepository.findById(paiementId)
//                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("montant", paiement.getMontant());
//        metadata.put("mois", paiement.getMois());
//        metadata.put("annee", paiement.getAnnee());
//        metadata.put("datePaiement", paiement.getDatePaiement().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Paiement confirmé")
//                .message("Votre paiement de " + paiement.getMontant() + " DH pour " +
//                        paiement.getMois() + " " + paiement.getAnnee() + " a été confirmé.")
//                .type("PAYMENT_CONFIRMED")
//                .referenceId(paiementId)
//                .referenceType("PAIEMENT")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationPaiementEnRetard(Long paiementId, Long locataireId) {
//        Paiement paiement = paiementRepository.findById(paiementId)
//                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("montant", paiement.getMontant());
//        metadata.put("mois", paiement.getMois());
//        metadata.put("annee", paiement.getAnnee());
//        metadata.put("joursRetard", paiement.getJoursRetard());
//        metadata.put("dateEcheance", paiement.getDateEcheance().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Paiement en retard")
//                .message("ATTENTION : Votre paiement de " + paiement.getMontant() + " DH pour " +
//                        paiement.getMois() + " " + paiement.getAnnee() + " est en retard de " +
//                        paiement.getJoursRetard() + " jours.")
//                .type("PAYMENT_OVERDUE")
//                .referenceId(paiementId)
//                .referenceType("PAIEMENT")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationReclamationCreee(Long reclamationId, Long locataireId) {
//        Reclamation reclamation = reclamationRepository.findById(reclamationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Réclamation non trouvée"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("titre", reclamation.getTitre());
//        metadata.put("urgence", reclamation.getUrgence());
//        metadata.put("dateCreation", reclamation.getCreatedAt().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Réclamation créée")
//                .message("Votre réclamation \"" + reclamation.getTitre() + "\" a été créée avec succès.")
//                .type("RECLAMATION_CREATED")
//                .referenceId(reclamationId)
//                .referenceType("RECLAMATION")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationReclamationRepondue(Long reclamationId, Long locataireId) {
//        Reclamation reclamation = reclamationRepository.findById(reclamationId)
//                .orElseThrow(() -> new ResourceNotFoundException("Réclamation non trouvée"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("titre", reclamation.getTitre());
//        metadata.put("statut", reclamation.getStatut());
//        metadata.put("dateReponse", reclamation.getDateResolution().toString());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Réponse à votre réclamation")
//                .message("Une réponse a été apportée à votre réclamation \"" +
//                        reclamation.getTitre() + "\".")
//                .type("RECLAMATION_RESPONSE")
//                .referenceId(reclamationId)
//                .referenceType("RECLAMATION")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationContratExpiration(Long contratId, Long locataireId) {
//        Contrat contrat = contratRepository.findById(contratId)
//                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("dateFin", contrat.getDateFin().toString());
//        metadata.put("joursRestants", contrat.getJoursRestants());
//        metadata.put("bienReference", contrat.getBien().getReference());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Contrat arrivant à expiration")
//                .message("Votre contrat pour le bien " + contrat.getBien().getReference() +
//                        " expire dans " + contrat.getJoursRestants() + " jours.")
//                .type("CONTRACT_EXPIRY")
//                .referenceId(contratId)
//                .referenceType("CONTRAT")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    @Override
//    @Transactional
//    public void creerNotificationVisitePlanifiee(Long visiteId, Long locataireId) {
//        Visite visite = visiteRepository.findById(visiteId)
//                .orElseThrow(() -> new ResourceNotFoundException("Visite non trouvée"));
//
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("dateVisite", visite.getDateVisite().toString());
//        metadata.put("typeVisite", visite.getTypeVisite());
//        metadata.put("bienReference", visite.getBien().getReference());
//
//        DemandeNotificationRequestDto notificationDto = DemandeNotificationRequestDto.builder()
//                .utilisateurId(locataireId)
//                .titre("Visite planifiée")
//                .message("Une visite " + visite.getTypeVisite() + " est prévue pour le " +
//                        visite.getDateVisite().toLocalDate() + " pour le bien " +
//                        visite.getBien().getReference() + ".")
//                .type("VISITE_PLANNED")
//                .referenceId(visiteId)
//                .referenceType("VISITE")
//                .metadata(metadata)
//                .build();
//
//        creerNotification(notificationDto);
//    }
//
//    // =========================================================================
//    // MÉTHODES PRIVÉES
//    // =========================================================================
//
//    private DemandeNotificationResponseDto mapToDto(Notification notification) {
//        DemandeNotificationResponseDto dto = modelMapper.map(notification, DemandeNotificationResponseDto.class);
//
//        // Mapper les informations de l'utilisateur
//        dto.setUtilisateurId(notification.getUtilisateur().getId());
//        dto.setUtilisateurNom(notification.getUtilisateur().getNomComplet());
//        dto.setUtilisateurEmail(notification.getUtilisateur().getEmail());
//
//        return dto;
//    }
//
//    @Transactional
//    public void nettoyerAnciennesNotifications(int joursConservation) {
//        LocalDateTime dateLimite = LocalDateTime.now().minusDays(joursConservation);
//
//        // Récupérer tous les utilisateurs
//        List<User> utilisateurs = userRepository.findAll();
//
//        for (User utilisateur : utilisateurs) {
//            notificationRepository.deleteOldNotifications(utilisateur.getId(), dateLimite);
//        }
//
//        log.info("Nettoyage des notifications plus anciennes que {} jours effectué", joursConservation);
//    }
//}