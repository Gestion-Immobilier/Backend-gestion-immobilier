package univh2.fstm.gestionimmobilier.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Service de géocodage via l'API Nominatim (OpenStreetMap).
 *
 * ✅ 100% gratuit, aucune clé API requise.
 * ✅ Retourne toujours un Optional : si le géocodage échoue, le bien est
 *    créé quand même sans coordonnées (jamais bloquant).
 *
 * Respect des CGU Nominatim : User-Agent identifié, pas de spam de requêtes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?q={query}&format=json&limit=1&addressdetails=0";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Géocode une adresse complète.
     *
     * @param adresse    Rue et numéro (ex: "25 Rue Hassan II")
     * @param ville      Ville (ex: "Fès")
     * @param codePostal Code postal (ex: "30000")
     * @return Optional contenant [latitude, longitude] si trouvé, vide sinon.
     */
    public Optional<double[]> geocoderAdresse(String adresse, String ville, String codePostal) {
        // Tentative 1 : adresse complète avec code postal
        Optional<double[]> result = tenterGeocodage(
                String.format("%s, %s, %s, Maroc", adresse, ville, codePostal));
        if (result.isPresent()) return result;

        // Tentative 2 : sans code postal (Nominatim reconnaît mieux les noms de lieux)
        result = tenterGeocodage(String.format("%s, %s, Maroc", adresse, ville));
        if (result.isPresent()) return result;

        // Tentative 3 : uniquement la ville (coordonnées du centre-ville au pire)
        result = tenterGeocodage(String.format("%s, Maroc", ville));
        if (result.isPresent()) {
            log.warn("⚠️ Géocodage approximatif (centre-ville) pour: {} {}", adresse, ville);
            return result;
        }

        log.warn("⚠️ Impossible de géocoder: {}, {}", adresse, ville);
        return Optional.empty();
    }

    private Optional<double[]> tenterGeocodage(String query) {
        try {
            log.info("🌍 Tentative Nominatim: {}", query);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "GestionImmobilierApp/1.0");
            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<NominatimResult[]> response =
                    restTemplate.exchange(
                            NOMINATIM_URL,
                            org.springframework.http.HttpMethod.GET,
                            entity,
                            NominatimResult[].class,
                            query
                    );

            NominatimResult[] results = response.getBody();
            if (results != null && results.length > 0) {
                double lat = Double.parseDouble(results[0].getLat());
                double lon = Double.parseDouble(results[0].getLon());
                log.info("✅ Coordonnées trouvées: lat={}, lon={}", lat, lon);
                return Optional.of(new double[]{lat, lon});
            }
            return Optional.empty();

        } catch (Exception e) {
            log.warn("⚠️ Géocodage échoué ({}): {}", query, e.getMessage());
            return Optional.empty();
        }
    }

    // DTO interne pour désérialiser la réponse JSON de Nominatim
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    static class NominatimResult {
        private String lat;
        private String lon;

        public String getLat() { return lat; }
        public String getLon() { return lon; }
        public void setLat(String lat) { this.lat = lat; }
        public void setLon(String lon) { this.lon = lon; }
    }
}
