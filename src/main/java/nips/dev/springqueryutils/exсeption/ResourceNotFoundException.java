package nips.dev.springqueryutils.exсeption;

/**
 * Запись не найдена: нет такого id или она уже soft-deleted.
 *
 * <p>С {@link RestExceptionHandler} — HTTP 404.
 *
 * @author nip
 * @since 0.0.1
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s with id %s not found", resourceName, id));
    }
}
