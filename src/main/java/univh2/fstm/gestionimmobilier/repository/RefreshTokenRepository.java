package univh2.fstm.gestionimmobilier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.RefreshToken;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    Optional<RefreshToken> findByPersonne(Personne personne);
    
    int deleteByPersonne(Personne personne);
}
