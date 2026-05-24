package nips.dev.springqueryutils.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ list для контроллера: данные, {@code total}, есть ли следующая/предыдущая страница.
 *
 * @param <T> чаще всего {@code List<ItemDto>} или {@code List<Item>}
 * @author nip
 * @since 0.0.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageableResult<T> {
    private T data;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    /**
     * Собирает ответ; {@code totalPages}, {@code hasNext}, {@code hasPrevious} считаются сами.
     */
    public static <T> PageableResult<T> of(T data, long total, int page, int size) {
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageableResult<>(
                data,
                total,
                page,
                size,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }
}
