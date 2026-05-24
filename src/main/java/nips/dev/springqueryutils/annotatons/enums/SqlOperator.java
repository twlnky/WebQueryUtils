package nips.dev.springqueryutils.annotatons.enums;

/**
 * Оператор в середине строки фильтра: {@code поле:ОПЕРАТОР:значение}.
 *
 * <p>В URL пишется как в enum: {@code LIKE}, {@code GREATER}, {@code IN} и т.д.
 *
 * @author nip
 * @since 0.0.1
 */
public enum SqlOperator {
    EQUALS,
    NOT_EQUALS,
    LIKE,
    GREATER,
    LESS,
    IN,
    IS_NULL
}
