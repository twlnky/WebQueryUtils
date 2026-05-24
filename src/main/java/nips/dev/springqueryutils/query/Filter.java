package nips.dev.springqueryutils.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Список условий с API. Обычно кладёте туда значения из {@code ?filter=...&filter=...}.
 *
 * <p>Все строки в одном {@link Filter} объединяются через AND.
 *
 * @author nip
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
public class Filter {
    private List<String> filter = new ArrayList<>();
}