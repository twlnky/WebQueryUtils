package nips.dev.springqueryutils.query;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import nips.dev.springqueryutils.annotatons.FilterFieldAllies;
import nips.dev.springqueryutils.annotatons.enums.SqlOperator;
import nips.dev.springqueryutils.exсeption.ValidationException;
import nips.dev.springqueryutils.template.EntityMetadata;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Собирает JPA {@link Specification} из строк фильтра с API.
 *
 * <p>Формат одной строки: {@code поле:ОПЕРАТОР:значение}. Режем по первым двум {@code :}, всё после второго —
 * это value (в value могут быть свои двоеточия). Несколько строк в {@link Filter} — всегда AND, не OR.
 *
 * <p>Имя поля должно быть в whitelist entity (см. {@link EntityMetadata}). Иначе — {@link nips.dev.springqueryutils.exсeption.ValidationException}.
 *
 * @author nip
 * @since 0.0.1
 */
public class FilterSpecificationBuilder {

    private FilterSpecificationBuilder() {
    }

    /**
     * Удобно для тестов: передаёте класс entity, метаданные соберутся сами.
     */
    public static <T> Specification<T> build(Filter filter, Class<T> entityClass) {
        return build(filter, EntityMetadata.of(entityClass));
    }

    /** То же, что {@link #build(Filter, Class)}, но метаданные уже есть (как в {@link AbstractCRUDLService}). */
    public static <T> Specification<T> build(Filter filter, EntityMetadata<T> metadata) {
        if (filter == null || filter.getFilter() == null || filter.getFilter().isEmpty()) {
            return (root, query, cb) -> null;
        }

        List<Specification<T>> specifications = new ArrayList<>();
        Map<String, Field> filterableFields = metadata.getFilterableFields();

        for (String filterString : filter.getFilter()) {
            String[] parts = filterString.split(":", 3);
            if (parts.length < 2) {
                throw new ValidationException("Invalid filter format. Expected: field:operator[:value]");
            }

            String fieldKey = parts[0].trim();
            String operatorStr = parts[1].trim();
            String value = parts.length > 2 ? parts[2] : "";

            Field field = filterableFields.get(fieldKey);
            if (field == null) {
                throw new ValidationException("Field '" + fieldKey + "' is not filterable");
            }

            FilterFieldAllies annotation = field.getAnnotation(FilterFieldAllies.class);
            String propertyPath = annotation != null && !annotation.value().isEmpty()
                    ? annotation.value()
                    : field.getName();
            SqlOperator operator = parseOperator(operatorStr, annotation.operator());

            specifications.add(createSpecification(propertyPath, field.getType(), operator, value, fieldKey));
        }

        return specifications.stream()
                .reduce(Specification::and)
                .orElse((root, query, cb) -> null);
    }

    private static SqlOperator parseOperator(String operatorStr, SqlOperator defaultOperator) {
        if (operatorStr.isEmpty()) {
            return defaultOperator;
        }
        try {
            return SqlOperator.valueOf(operatorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown operator: " + operatorStr);
        }
    }

    private static <T> Specification<T> createSpecification(
            String propertyPath,
            Class<?> javaType,
            SqlOperator operator,
            String value,
            String fieldKey
    ) {
        if (operator == SqlOperator.LIKE && !String.class.isAssignableFrom(javaType)) {
            throw new ValidationException("LIKE operator requires a String field: " + fieldKey);
        }

        return (root, query, cb) -> {
            Path<?> path = getFieldPath(root, propertyPath);

            return switch (operator) {
                case EQUALS -> cb.equal(path, convertValue(javaType, value, fieldKey));
                case NOT_EQUALS -> cb.notEqual(path, convertValue(javaType, value, fieldKey));
                case LIKE -> cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");
                case GREATER -> greaterThan(path, javaType, value, fieldKey, cb);
                case LESS -> lessThan(path, javaType, value, fieldKey, cb);
                case IN -> path.in(parseInValues(javaType, value, fieldKey));
                case IS_NULL -> cb.isNull(path);
            };
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static jakarta.persistence.criteria.Predicate greaterThan(
            Path<?> path,
            Class<?> javaType,
            String value,
            String fieldKey,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        Comparable converted = (Comparable) convertValue(javaType, value, fieldKey);
        jakarta.persistence.criteria.Expression<Comparable> expression =
                (jakarta.persistence.criteria.Expression<Comparable>) path;
        return cb.greaterThan(expression, converted);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static jakarta.persistence.criteria.Predicate lessThan(
            Path<?> path,
            Class<?> javaType,
            String value,
            String fieldKey,
            jakarta.persistence.criteria.CriteriaBuilder cb
    ) {
        Comparable converted = (Comparable) convertValue(javaType, value, fieldKey);
        jakarta.persistence.criteria.Expression<Comparable> expression =
                (jakarta.persistence.criteria.Expression<Comparable>) path;
        return cb.lessThan(expression, converted);
    }

    private static List<Object> parseInValues(Class<?> javaType, String value, String fieldKey) {
        if (value.isBlank()) {
            throw new ValidationException("IN operator requires at least one value for field: " + fieldKey);
        }
        String[] values = value.split(",");
        List<Object> convertedValues = new ArrayList<>();
        for (String item : values) {
            convertedValues.add(convertValue(javaType, item.trim(), fieldKey));
        }
        return convertedValues;
    }

    private static Path<?> getFieldPath(Root<?> root, String fieldName) {
        String[] parts = fieldName.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private static Object convertValue(Class<?> javaType, String value, String fieldKey) {
        if (value == null) {
            throw new ValidationException("Value is required for field: " + fieldKey);
        }
        try {
            if (javaType == String.class) {
                return value;
            }
            if (javaType == Long.class || javaType == long.class) {
                return Long.parseLong(value);
            }
            if (javaType == Integer.class || javaType == int.class) {
                return Integer.parseInt(value);
            }
            if (javaType == Double.class || javaType == double.class) {
                return Double.parseDouble(value);
            }
            if (javaType == Float.class || javaType == float.class) {
                return Float.parseFloat(value);
            }
            if (javaType == Boolean.class || javaType == boolean.class) {
                return Boolean.parseBoolean(value);
            }
            if (javaType == BigDecimal.class) {
                return new BigDecimal(value);
            }
            if (javaType == UUID.class) {
                return UUID.fromString(value);
            }
            if (javaType == LocalDate.class) {
                return LocalDate.parse(value);
            }
            if (javaType == LocalDateTime.class) {
                return LocalDateTime.parse(value);
            }
            if (javaType == Instant.class) {
                return Instant.parse(value);
            }
            if (javaType.isEnum()) {
                return Enum.valueOf((Class<? extends Enum>) javaType, value);
            }
            return value;
        } catch (RuntimeException e) {
            throw new ValidationException(
                    "Cannot convert value '" + value + "' for field '" + fieldKey + "': " + e.getMessage()
            );
        }
    }
}
