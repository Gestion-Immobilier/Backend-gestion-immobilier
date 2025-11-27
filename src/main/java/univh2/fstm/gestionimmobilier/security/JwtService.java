package univh2.fstm.gestionimmobilier.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import univh2.fstm.gestionimmobilier.model.Personne;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    // Une clé de 32+ caractères (HS256)
    private static final String SECRET_KEY = "12345678901234567890123456789012";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ------------------------------
    // 🔹 Génération du JWT
    // ------------------------------
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

    // ------------------------------
    // 🔹 Extraire l'email (username)
    // ------------------------------
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ------------------------------
    // 🔹 Extraire toutes les claims
    // ------------------------------
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ------------------------------
    // 🔹 Vérifier si expiré
    // ------------------------------
    public boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ------------------------------
    // Vérifier validité du token avec UserDetails
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractEmail(token)) && !isExpired(token);
    }


}
