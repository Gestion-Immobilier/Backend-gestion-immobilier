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

    private static final String SECRET_KEY = "12345678901234567890123456789012";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
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
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractEmail(token)) && !isExpired(token);
    }


    // nouvelles methodes pour recuperer id depuis le token

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object idClaim = claims.get("id");

        if (idClaim instanceof Integer) {
            return ((Integer) idClaim).longValue();
        } else if (idClaim instanceof Long) {
            return (Long) idClaim;
        } else if (idClaim instanceof String) {
            return Long.parseLong((String) idClaim);
        }

        return null;
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }







}
