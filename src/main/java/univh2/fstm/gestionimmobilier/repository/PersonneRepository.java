package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.Type;

import java.util.List;

public interface PersonneRepository extends JpaRepository<Personne, Long> {
    List<Personne> findByType(Type type);
    Personne findByEmail(String email);
    Personne findByPhone(String phone);
    List<Personne> findByAdresse(String adresse);
    List<Personne> findByFirstName(String firstName);
    List<Personne> findByLastName(String lastName);
}
