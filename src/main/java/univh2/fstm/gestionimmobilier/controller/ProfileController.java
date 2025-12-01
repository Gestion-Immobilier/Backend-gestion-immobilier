package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.request.ChangePasswordRequest;
import univh2.fstm.gestionimmobilier.dto.request.UpdateProfileRequest;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.service.impl.ProfileService;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // ADMIN peut voir tous les profils
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Personne> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    // Tous les rôles peuvent voir leur propre profil
    @PreAuthorize("hasAnyRole('ADMIN','LOCATAIRE','PROPRIETAIRE')")
    @GetMapping("/me")
    public Personne getProfile(Authentication auth) {
        return profileService.getProfile(auth.getName());
    }

    // Mise à jour profil : chacun son profil
    @PreAuthorize("hasAnyRole('ADMIN','LOCATAIRE','PROPRIETAIRE')")
    @PutMapping("/update")
    public Personne updateProfile(@RequestBody UpdateProfileRequest request,
                                  Authentication auth) {
        return profileService.updateProfile(auth.getName(), request);
    }
    // Changer mot de passe
    @PreAuthorize("hasAnyRole('ADMIN','LOCATAIRE','PROPRIETAIRE')")
    @PutMapping("/password")
    public String changePassword(@RequestBody ChangePasswordRequest request,
                                 Authentication auth) {
        profileService.changePassword(auth.getName(), request);
        return "Mot de passe changé avec succès";
    }

    // Supprimer son propre profil
    @PreAuthorize("hasAnyRole('ADMIN','LOCATAIRE','PROPRIETAIRE')")
    @DeleteMapping("/delete")
    public String deleteProfile(Authentication auth) {
        profileService.deleteProfile(auth.getName());
        return "Compte supprimé avec succès";
    }
    @PreAuthorize("hasRole('LOCATAIRE')")
    @PostMapping("/request-proprietaire")
    public String demanderProprietaire(Authentication auth) {
        profileService.demanderProprietaire(auth.getName());
        return "Votre demande a été envoyée à l'administrateur.";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/validate-proprietaire/{id}")
    public String validerProprietaire(@PathVariable Long id) {
        profileService.validerProprietaire(id);
        return "Demande validée !";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject-proprietaire/{id}")
    public String rejeterProprietaire(@PathVariable Long id) {
        profileService.rejeterProprietaire(id);
        return "Demande rejetée !";
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve-proprietaire/{userId}")
    public String approuverProprietaire(@PathVariable Long userId) {
        profileService.approuverProprietaire(userId);
        return "Demande approuvée";
    }



}
