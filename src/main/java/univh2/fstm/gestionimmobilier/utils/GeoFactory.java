package univh2.fstm.gestionimmobilier.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

/**
 * Utilitaire Spring pour créer des objets géométriques JTS compatibles PostGIS.
 *
 * SRID 4326 = WGS84 = système de coordonnées GPS mondial (Google Maps, OpenStreetMap).
 *
 * ⚠️ En JTS : X = longitude, Y = latitude (ordre contre-intuitif !)
 */
@Component
public class GeoFactory {

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Crée un Point PostGIS à partir de coordonnées GPS.
     *
     * @param latitude  Latitude GPS  (ex: 34.0331 pour Fès)
     * @param longitude Longitude GPS (ex: -5.0003 pour Fès)
     * @return Point JTS prêt à être stocké en base via Hibernate Spatial
     */
    public Point creerPoint(double latitude, double longitude) {
        // JTS Coordinate : (X=longitude, Y=latitude) — ne pas inverser !
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
