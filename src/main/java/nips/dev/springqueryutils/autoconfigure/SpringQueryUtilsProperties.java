package nips.dev.springqueryutils.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки из {@code application.properties} / YAML с префиксом {@code spring.query-utils}.
 *
 * <p>Главное для list: {@code max-page-size} — верхняя граница {@code size} в запросе.
 *
 * @author nip
 * @since 0.0.1
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "spring.query-utils")
public class SpringQueryUtilsProperties {

    private boolean enabled = true;
    private int maxPageSize = 100;
    private boolean exceptionHandlerEnabled = true;

}
