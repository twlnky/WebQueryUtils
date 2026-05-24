package nips.dev.springqueryutils.exсeption;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void mapsNotFoundTo404ProblemDetail() {
        ProblemDetail problem = handler.handleNotFound(new ResourceNotFoundException("TestItem", 42L));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Resource not found");
        assertThat(problem.getDetail()).contains("42");
    }

    @Test
    void mapsValidationTo400ProblemDetail() {
        ValidationException ex = new ValidationException("bad request", Map.of("field", "invalid"));
        ProblemDetail problem = handler.handleValidation(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getTitle()).isEqualTo("Validation failed");
        assertThat(problem.getProperties()).containsEntry("errors", Map.of("field", "invalid"));
    }
}
