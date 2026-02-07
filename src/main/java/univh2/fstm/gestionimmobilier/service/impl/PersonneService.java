package univh2.fstm.gestionimmobilier.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.dto.response.PersonneDTO;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PersonneService {

    private final PersonneRepository personneRepository;

    private final PasswordEncoder passwordEncoder;

    public Personne create(Personne personne) {
        personne.setPassword(passwordEncoder.encode(personne.getPassword()));
        return personneRepository.save(personne);
    }

    // CRUD de base
    public Personne ajouterPersonne(PersonneDTO dto) {
        Personne personne = new Personne();
        personne.setFirstName(dto.getFirstName());
        personne.setLastName(dto.getLastName());
        personne.setEmail(dto.getEmail());
        personne.setPhone(dto.getPhone());
        personne.setType(dto.getType());
        personne.setAdresse(dto.getAdresse());

        // Encodage du mot de passe
        personne.setPassword(passwordEncoder.encode(dto.getPassword()));
        return personneRepository.save(personne);
    }

    public Personne modifierPersonne(Personne personne) {
        return personneRepository.save(personne);
    }

    public void supprimerPersonne(Long id) {
        personneRepository.deleteById(id);
    }

    public List<Personne> findAll() {
        return personneRepository.findAll();
    }

    public Personne findById(Long id) {
        return personneRepository.findById(id).orElse(null);
    }

    // Recherches par attributs
    public Personne findByEmail(String email) {
        return personneRepository.findByEmail(email)
                .orElse(null);
    }

    public Personne findByPhone(String phone) {
        return personneRepository.findByPhone(phone);
    }

    public List<Personne> findByAdresse(String adresse) {
        return personneRepository.findByAdresse(adresse);
    }

    public List<Personne> findByType(Type type) {
        return personneRepository.findByType(type);
    }

    public List<Personne> findByFirstName(String firstName) {
        return personneRepository.findByFirstName(firstName);
    }

    public List<Personne> findByLastName(String lastName) {
        return personneRepository.findByLastName(lastName);
    }


}
