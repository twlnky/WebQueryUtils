package nips.dev.springqueryutils.autoconfigure;

import nips.dev.springqueryutils.exсeption.RestExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Web-часть starter'а: {@link RestExceptionHandler}. Подключается только если в приложении есть Spring MVC.
 *
 * @author nip
 * @since 0.0.1
 */
@AutoConfiguration
@ConditionalOnClass(RestControllerAdvice.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "spring.query-utils", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SpringQueryUtilsWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "spring.query-utils",
            name = "exception-handler-enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public RestExceptionHandler springQueryUtilsRestExceptionHandler() {
        return new RestExceptionHandler();
    }
}
