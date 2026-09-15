package univh2.fstm.gestionimmobilier.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import univh2.fstm.gestionimmobilier.dto.response.PersonneDTO;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;
import univh2.fstm.gestionimmobilier.service.impl.PersonneService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/personnes")
@RequiredArgsConstructor
public class PersonneController {

    private final PersonneService personneService;

    // ----------------- CRUD -----------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Personne ajouterPersonne(@RequestBody PersonneDTO personne) {
        return personneService.ajouterPersonne(personne);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Personne modifierPersonne(@PathVariable Long id, @RequestBody Personne personne) {
        personne.setId(id);
        return personneService.modifierPersonne(personne);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimerPersonne(@PathVariable Long id) {
        personneService.supprimerPersonne(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> afficherTousPersonnes() {
        return personneService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Personne afficherPersonne(@PathVariable Long id) {
        return personneService.findById(id);
    }

    // ----------------- Filtrage -----------------

    @GetMapping("/locataires")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> afficherTousLocataires() {
        return personneService.findByType(Type.LOCATAIRE);
    }

    @GetMapping("/proprietaires")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> afficherTousProprietaires() {
        return personneService.findByType(Type.PROPRIETAIRE);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> afficherTousAdmins() {
        return personneService.findByType(Type.ADMIN);
    }

    // ----------------- Recherche par attribut -----------------

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public Personne findByEmail(@PathVariable String email) {
        return personneService.findByEmail(email);
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasRole('ADMIN')")
    public Personne findByPhone(@PathVariable String phone) {
        return personneService.findByPhone(phone);
    }

    @GetMapping("/adresse/{adresse}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> findByAdresse(@PathVariable String adresse) {
        return personneService.findByAdresse(adresse);
    }

    @GetMapping("/firstname/{firstName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> findByFirstName(@PathVariable String firstName) {
        return personneService.findByFirstName(firstName);
    }

    @GetMapping("/lastname/{lastName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Personne> findByLastName(@PathVariable String lastName) {
        return personneService.findByLastName(lastName);
    }

    // ----------------- Validation Locataire -----------------

    @PutMapping("/{id}/valider-locataire")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Valider un locataire", description = "Vérifie et valide le compte d'un locataire (ADMIN uniquement)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locataire validé avec succès"),
            @ApiResponse(responseCode = "404", description = "Locataire introuvable"),
            @ApiResponse(responseCode = "400", description = "L'utilisateur n'est pas un locataire")
    })
    public Personne validerLocataire(@PathVariable Long id) {
        return personneService.validerLocataire(id);
    }

}
