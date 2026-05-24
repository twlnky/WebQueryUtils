package nips.dev.springqueryutils.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@code page} и {@code size} для {@code list}. По умолчанию страница 0, размер 20.
 *
 * <p>Размер не может быть больше {@code spring.query-utils.max-page-size}.
 *
 * @author nip
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    private int page = 0;
    private int size = 20;

}

