package nips.dev.springqueryutils.annotatons;

import nips.dev.springqueryutils.annotatons.enums.SqlOperator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Поле можно фильтровать из API и сортировать в {@code list}.
 *
 * <p>На поле {@code name} часто вешают {@code alias = "itemName"}, чтобы в URL было
 * {@code ?filter=itemName:LIKE:foo}, а в БД шло в колонку {@code name}.
 *
 * @author nip
 * @since 0.0.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FilterFieldAllies {

    /**
     * Путь в entity для вложенных полей, например {@code "category.name"}.
     */
    String value() default "";

    /** Имя в query-string; пусто — берётся имя Java-поля. */
    String alias() default "";

    /** Если в строке фильтра оператор пропущен ({@code score::10}), подставится этот. */
    SqlOperator operator() default SqlOperator.EQUALS;
}
