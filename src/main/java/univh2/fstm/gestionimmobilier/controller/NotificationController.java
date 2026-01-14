//// NotificationController.java
//package univh2.fstm.gestionimmobilier.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import univh2.fstm.gestionimmobilier.dto.request.DemandeNotificationRequestDto;
//import univh2.fstm.gestionimmobilier.dto.response.DemandeNotificationResponseDto;
//import univh2.fstm.gestionimmobilier.repository.NotificationRepository;
//import univh2.fstm.gestionimmobilier.service.interfaces.NotificationService;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/notifications")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Notifications", description = "API de gestion des notifications")
//public class NotificationController {
//
//    private final NotificationRepository notificationService;
//
//    @GetMapping("/mes-notifications")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Récupérer mes notifications")
//    public ResponseEntity<List<DemandeNotificationResponseDto>> getMesNotifications() {
//        Long utilisateurId = getCurrentUserId(); // À implémenter
//        List<DemandeNotificationResponseDto> notifications =
//            notificationService.getNotificationsByUser(utilisateurId);
//        return ResponseEntity.ok(notifications);
//    }
//
//    @GetMapping("/non-lues")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Récupérer mes notifications non lues")
//    public ResponseEntity<List<DemandeNotificationResponseDto>> getNotificationsNonLues() {
//        Long utilisateurId = getCurrentUserId();
//        List<DemandeNotificationResponseDto> notifications =
////            notificationService.getNotificationsNonLues(utilisateurId);
//        return ResponseEntity.ok(notifications);
//    }
//
//    @GetMapping("/{id}")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Récupérer une notification par ID")
//    public ResponseEntity<DemandeNotificationResponseDto> getNotificationById(@PathVariable Long id) {
//        DemandeNotificationResponseDto notification = notificationService.getNotificationById(id);
//        return ResponseEntity.ok(notification);
//    }
//
//    @PatchMapping("/{id}/marquer-lue")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Marquer une notification comme lue")
//    public ResponseEntity<DemandeNotificationResponseDto> marquerCommeLue(@PathVariable Long id) {
//        DemandeNotificationResponseDto notification = notificationService.marquerCommeLue(id);
//        return ResponseEntity.ok(notification);
//    }
//
//    @PatchMapping("/marquer-tout-lu")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Marquer toutes mes notifications comme lues")
//    public ResponseEntity<Void> marquerToutCommeLu() {
//        Long utilisateurId = getCurrentUserId();
//        notificationService.marquerToutCommeLu(utilisateurId);
//        return ResponseEntity.ok().build();
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Supprimer une notification")
//    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
//        notificationService.deleteNotification(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @DeleteMapping("/effacer-tout")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Effacer toutes mes notifications")
//    public ResponseEntity<Void> deleteAllNotifications() {
//        Long utilisateurId = getCurrentUserId();
//        notificationService.deleteAllByUser(utilisateurId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @GetMapping("/stats/non-lues")
//    @PreAuthorize("isAuthenticated()")
//    @Operation(summary = "Compter les notifications non lues")
//    public ResponseEntity<Map<String, Long>> countNotificationsNonLues() {
//        Long utilisateurId = getCurrentUserId();
//        long count = notificationService.countNotificationsNonLues(utilisateurId);
//
//        return ResponseEntity.ok(Map.of("count", count));
//    }
//
//    // Méthode utilitaire pour récupérer l'ID de l'utilisateur connecté
//    private Long getCurrentUserId() {
//        // À implémenter selon votre système d'authentification
//        // Exemple avec SecurityContext
//        return 1L; // Remplacer par l'ID réel
//    }
//}