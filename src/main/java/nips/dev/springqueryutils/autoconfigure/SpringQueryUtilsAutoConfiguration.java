package nips.dev.springqueryutils.autoconfigure;

import jakarta.persistence.EntityManager;
import nips.dev.springqueryutils.dto.DtoMapper;
import nips.dev.springqueryutils.dto.EntityDtoMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Подключает starter, когда в проекте есть Spring Data JPA.
 *
 * <p>Создаёт {@link DtoMapper}. Для REST-ошибок см. {@link SpringQueryUtilsWebAutoConfiguration}
 * (нужен {@code spring-boot-starter-web} в приложении). Выключить всё: {@code spring.query-utils.enabled=false}.
 *
 * @author nip
 * @since 0.0.1
 */
@AutoConfiguration
@ConditionalOnClass({JpaRepository.class, EntityManager.class})
@EnableConfigurationProperties(SpringQueryUtilsProperties.class)
@ConditionalOnProperty(prefix = "spring.query-utils", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SpringQueryUtilsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DtoMapper dtoMapper(List<EntityDtoMapper<?, ?>> mappers) {
        return new DtoMapper(mappers);
    }

}
