package nips.dev.springqueryutils.query;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import nips.dev.springqueryutils.annotatons.FilterFieldAllies;
import nips.dev.springqueryutils.annotatons.enums.SqlOperator;
import nips.dev.springqueryutils.exсeption.ValidationException;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterSpecificationBuilder {

    public static <T> Specification<T> build(Filter filter, Class<T> entityClass) {
        if (filter == null || filter.getFilter() == null || filter.getFilter().isEmpty()) {
            return (root, query, cb) -> null;
        }

        List<Specification<T>> specifications = new ArrayList<>();
        Map<String, Field> filterableFields = getFilterableFields(entityClass);

        for (String filterString : filter.getFilter()) {
            String[] parts = filterString.split(":", 3);
            if (parts.length < 2) {
                throw new ValidationException("Invalid filter format. Expected: field:operator:value");
            }

            String fieldName = parts[0];
            String operatorStr = parts[1];
            String value = parts.length > 2 ? parts[2] : "";

            Field field = filterableFields.get(fieldName);
            if (field == null) {
                throw new ValidationException("Field '" + fieldName + "' is not filterable");
            }

            FilterFieldAllies annotation = field.getAnnotation(FilterFieldAllies.class);
            String dbFieldName = annotation.value().isEmpty() ? fieldName : annotation.value();
            SqlOperator operator = parseOperator(operatorStr, annotation.operator());

            specifications.add(createSpecification(dbFieldName, operator, value));
        }

        return specifications.stream()
                .reduce(Specification::and)
                .orElse((root, query, cb) -> null);
    }

    private static <T> Map<String, Field> getFilterableFields(Class<T> entityClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(FilterFieldAllies.class)) {
                    fields.add(field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return fields.stream()
                .collect(Collectors.toMap(Field::getName, field -> field));
    }

    private static SqlOperator parseOperator(String operatorStr, SqlOperator defaultOperator) {
        try {
            return SqlOperator.valueOf(operatorStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return defaultOperator;
        }
    }

    private static <T> Specification<T> createSpecification(String fieldName, SqlOperator operator, String value) {
        return (root, query, cb) -> {
            Path<?> field = getFieldPath(root, fieldName);
            return switch (operator) {
                case EQUALS -> cb.equal(field, convertValue(field, value));
                case LIKE -> cb.like(cb.lower(field.as(String.class)), "%" + value.toLowerCase() + "%");
                case GREATER -> {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comparableValue = (Comparable<Object>) convertValue(field, value);
                    @SuppressWarnings("unchecked")
                    jakarta.persistence.criteria.Expression<Comparable<Object>> comparableField = 
                            (jakarta.persistence.criteria.Expression<Comparable<Object>>) field;
                    yield cb.greaterThan(comparableField, comparableValue);
                }
                case LESS -> {
                    @SuppressWarnings("unchecked")
                    Comparable<Object> comparableValue = (Comparable<Object>) convertValue(field, value);
                    @SuppressWarnings("unchecked")
                    jakarta.persistence.criteria.Expression<Comparable<Object>> comparableField = 
                            (jakarta.persistence.criteria.Expression<Comparable<Object>>) field;
                    yield cb.lessThan(comparableField, comparableValue);
                }
                case IN -> {
                    String[] values = value.split(",");
                    List<Object> convertedValues = new ArrayList<>();
                    for (String v : values) {
                        convertedValues.add(convertValue(field, v.trim()));
                    }
                    yield field.in(convertedValues);
                }
            };
        };
    }

    private static Path<?> getFieldPath(Root<?> root, String fieldName) {
        String[] parts = fieldName.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private static Object convertValue(Path<?> field, String value) {
        Class<?> javaType = field.getJavaType();
        if (javaType == String.class) {
            return value;
        } else if (javaType == Long.class || javaType == long.class) {
            return Long.parseLong(value);
        } else if (javaType == Integer.class || javaType == int.class) {
            return Integer.parseInt(value);
        } else if (javaType == Double.class || javaType == double.class) {
            return Double.parseDouble(value);
        } else if (javaType == Boolean.class || javaType == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }
}
