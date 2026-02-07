package univh2.fstm.gestionimmobilier.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource,String field, Object value) {
        super(String.format("%s introuvable avec %s : '%s'", resource, field, value));
    }
}
