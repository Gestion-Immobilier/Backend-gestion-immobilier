package univh2.fstm.gestionimmobilier.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Personne;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    private static final String SECRET_KEY = "12345678901234567890123456789012";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Méthode pour nettoyer le token
    private String cleanToken(String token) {
        if (token == null) {
            return null;
        }
        // Supprime tous les espaces et caractères invisibles
        return token.trim()
                .replaceAll("\\s+", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n", "");
    }

    public String generateToken(Personne user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getType().name())
                .claim("id", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        try {
            String cleanedToken = cleanToken(token);
            return extractAllClaims(cleanedToken).getSubject();
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'email du token", e);
            return null;
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            String cleanedToken = cleanToken(token);

            // Log pour debug (à désactiver en production)
            logger.debug("Tentative de parsing du token (longueur: {}): {}...",
                    cleanedToken.length(),
                    cleanedToken.substring(0, Math.min(cleanedToken.length(), 20)));

            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(cleanedToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token expiré: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            logger.error("Token malformé: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            logger.error("Erreur de signature du token: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur lors du parsing du token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isExpired(String token) {
        try {
            String cleanedToken = cleanToken(token);
            return extractAllClaims(cleanedToken).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification d'expiration du token", e);
            return true; // En cas d'erreur, considérer comme invalide
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String cleanedToken = cleanToken(token);
            final String email = extractEmail(cleanedToken);
            return (email != null && email.equals(userDetails.getUsername())) && !isExpired(cleanedToken);
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token", e);
            return false;
        }
    }

    // Nouvelles méthodes pour récupérer id depuis le token
    public Long extractUserId(String token) {
        try {
            String cleanedToken = cleanToken(token);
            Claims claims = extractAllClaims(cleanedToken);
            Object idClaim = claims.get("id");

            if (idClaim instanceof Integer) {
                return ((Integer) idClaim).longValue();
            } else if (idClaim instanceof Long) {
                return (Long) idClaim;
            } else if (idClaim instanceof String) {
                return Long.parseLong((String) idClaim);
            }

            return null;
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'ID du token", e);
            return null;
        }
    }

    public String extractRole(String token) {
        try {
            String cleanedToken = cleanToken(token);
            return (String) extractAllClaims(cleanedToken).get("role");
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction du rôle du token", e);
            return null;
        }
    }
}