package nips.dev.springqueryutils.annotatons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Логическое удаление: поле {@code boolean} (обычно {@code deleted}).
 *
 * <p>{@code delete(id)} ставит {@code true}, строка в таблице остаётся.
 * {@code getById} / {@code list} / {@code update} работают только с {@code false}.
 *
 * @author nip
 * @since 0.0.1
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SoftDeleteFlag {
}
