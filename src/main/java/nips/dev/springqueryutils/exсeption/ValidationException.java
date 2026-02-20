package nips.dev.springqueryutils.exсeption;

import lombok.Getter;

import java.util.Map;

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
