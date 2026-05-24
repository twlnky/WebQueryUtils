package nips.dev.springqueryutils.exсeption;

import lombok.Getter;

import java.util.Map;

/**
 * Что-то не так с запросом: битый фильтр, слишком большая страница, чужое поле sort, нет MapStruct-маппера.
 *
 * <p>С {@link RestExceptionHandler} уходит на клиент как HTTP 400.
 *
 * @author nip
 * @since 0.0.1
 */
@Getter
public class ValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = Map.of();
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message);
        this.errors = errors;
    }

}
