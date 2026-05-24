package nips.dev.springqueryutils.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сортировка для {@code list}. Поле — только из разрешённых на entity (id и {@link nips.dev.springqueryutils.annotatons.FilterFieldAllies}).
 *
 * <p>Можно передать {@code null}, тогда порядок как отдаёт БД.
 *
 * @author nip
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sort {
    private String field;
    private SortDirection direction = SortDirection.ASC;

    public enum SortDirection {
        ASC, DESC
    }
}
