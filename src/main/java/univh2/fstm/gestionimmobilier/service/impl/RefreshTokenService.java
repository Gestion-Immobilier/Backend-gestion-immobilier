package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univh2.fstm.gestionimmobilier.model.Personne;
import univh2.fstm.gestionimmobilier.model.RefreshToken;
import univh2.fstm.gestionimmobilier.repository.PersonneRepository;
import univh2.fstm.gestionimmobilier.repository.RefreshTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PersonneRepository personneRepository;

    private final long refreshTokenDurationMs = 7L * 24 * 60 * 60 * 1000; // 7 days

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        Personne personne = personneRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));

        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByPersonne(personne);
        RefreshToken refreshToken;
        
        if (existingTokenOpt.isPresent()) {
            refreshToken = existingTokenOpt.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        } else {
            refreshToken = RefreshToken.builder()
                    .personne(personne)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                    .build();
        }

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Le refresh token a expiré. Veuillez vous reconnecter.");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        Personne personne = personneRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + userId));
        return refreshTokenRepository.deleteByPersonne(personne);
    }
}
