package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.PersonneDTO;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.service.PersonneService;

import java.util.List;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor
public class PersonneController {

    private final PersonneService personneService;

    // ----------------- CRUD -----------------
    @PostMapping
    public Personne ajouterPersonne(@RequestBody PersonneDTO personne) {
        return personneService.ajouterPersonne(personne);
    }

    @PutMapping("/{id}")
    public Personne modifierPersonne(@PathVariable Long id, @RequestBody Personne personne) {
        personne.setId(id);
        return personneService.modifierPersonne(personne);
    }

    @DeleteMapping("/{id}")
    public void supprimerPersonne(@PathVariable Long id) {
        personneService.supprimerPersonne(id);
    }

    @GetMapping
    public List<Personne> afficherTousPersonnes() {
        return personneService.findAll();
    }

    @GetMapping("/{id}")
    public Personne afficherPersonne(@PathVariable Long id) {
        return personneService.findById(id);
    }

    // ----------------- Filtrage -----------------
    @GetMapping("/locataires")
    public List<Personne> afficherTousLocataires() {
        return personneService.findByType(Type.LOCATAIRE);
    }

    @GetMapping("/proprietaires")
    public List<Personne> afficherTousProprietaires() {
        return personneService.findByType(Type.PROPRIETAIRE);
    }

    @GetMapping("/admin")
    public List<Personne> afficherTousAdmins() {
        return personneService.findByType(Type.ADMIN);
    }

    // ----------------- Recherche par attribut -----------------
    @GetMapping("/email/{email}")
    public Personne findByEmail(@PathVariable String email) {
        return personneService.findByEmail(email);
    }

    @GetMapping("/phone/{phone}")
    public Personne findByPhone(@PathVariable String phone) {
        return personneService.findByPhone(phone);
    }

    @GetMapping("/adresse/{adresse}")
    public List<Personne> findByAdresse(@PathVariable String adresse) {
        return personneService.findByAdresse(adresse);
    }

    @GetMapping("/firstname/{firstName}")
    public List<Personne> findByFirstName(@PathVariable String firstName) {
        return personneService.findByFirstName(firstName);
    }

    @GetMapping("/lastname/{lastName}")
    public List<Personne> findByLastName(@PathVariable String lastName) {
        return personneService.findByLastName(lastName);
    }
}
