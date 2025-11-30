package univh2.fstm.gestionimmobilier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.ChangePasswordRequest;
import univh2.fstm.gestionimmobilier.dto.UpdateProfileRequest;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PersonneRepository repo;
    private final PasswordEncoder passwordEncoder;

    // Récupérer le profil de l'utilisateur connecté
    public Personne getProfile(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // Mettre à jour le profil
    public Personne updateProfile(String email, UpdateProfileRequest request) {
        Personne user = getProfile(email);

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAdresse(request.getAdresse());

        return repo.save(user);
    }

    // Changer mot de passe
    public void changePassword(String email, ChangePasswordRequest request) {

        Personne user = getProfile(email);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        repo.save(user);
    }
    // ADMIN - voir tous les profils
    public List<Personne> getAllProfiles() {
        return repo.findAll();
    }

    // Supprimer son propre compte
    public void deleteProfile(String email) {
        Personne user = getProfile(email);
        repo.delete(user);
    }
    public void demanderProprietaire(String email) {
        Personne user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // On marque juste la demande, verified reste false
        user.setDemandeProprietaire(true);

        repo.save(user);
    }

    public void validerProprietaire(Long id) {
        Personne user = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (user.getDemandeProprietaire()) {
            user.setType(Type.PROPRIETAIRE);
            user.setVerified(true);        // ici seulement on valide
            user.setDemandeProprietaire(false); // on supprime la demande après validation
            repo.save(user);
        }
    }

    private Personne findById(Long id) {
        return repo.findById(id)
                .orElseThrow(null  );
    }

    public void rejeterProprietaire(Long id) {
        Personne p = findById(id);
        if (p == null) throw new RuntimeException("Utilisateur introuvable");

        p.setDemandeProprietaire(false);
        p.setVerified(false);

        repo.save(p);
    }
    public void approuverProprietaire(Long userId) {
        Personne user = repo.findById(userId).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (Boolean.TRUE.equals(user.getDemandeProprietaire())) {
            user.setType(Type.PROPRIETAIRE);
            user.setVerified(true);
            user.setDemandeProprietaire(false);
            repo.save(user);
        } else {
            throw new RuntimeException("Aucune demande en attente");
        }
    }


}
