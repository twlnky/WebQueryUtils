package nips.dev.springqueryutils.exсeption;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Отдаёт ошибки библиотеки клиенту как JSON {@link ProblemDetail} (404 и 400).
 *
 * <p>Работает, если в приложении есть {@code spring-boot-starter-web}.
 * Отключить: {@code spring.query-utils.exception-handler-enabled=false}.
 * Свой handler можно зарегистрировать вместо этого bean.
 *
 * @author nip
 * @since 0.0.1
 */
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidation(ValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Validation failed");
        if (!ex.getErrors().isEmpty()) {
            problem.setProperty("errors", ex.getErrors());
        }
        return problem;
    }
}
