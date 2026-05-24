package nips.dev.springqueryutils.template;

import jakarta.persistence.Id;
import nips.dev.springqueryutils.annotatons.FilterFieldAllies;
import nips.dev.springqueryutils.annotatons.SoftDeleteFlag;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Один раз смотрит на entity и запоминает: где {@link Id}, есть ли soft delete, какие поля можно
 * фильтровать и по каким сортировать.
 *
 * <p>Создаётся внутри {@link AbstractCRUDLService} при старте сервиса — на каждый HTTP-запрос
 * reflection заново не гоняется. В list попадают только поля с {@link FilterFieldAllies}.
 *
 * @param <M> ваша entity
 * @author nip
 * @since 0.0.1
 */
public final class EntityMetadata<M> {

    /**
     * Лимит {@code size}, если в properties не задали {@code spring.query-utils.max-page-size}.
     */
    public static final int DEFAULT_MAX_PAGE_SIZE = 100;

    private final Class<M> entityClass;
    private final Field idField;
    private final Field softDeleteField;
    private final Map<String, Field> filterableFields;
    private final Set<String> sortableKeys;
    private final Map<String, String> sortKeyToProperty;

    private EntityMetadata(
            Class<M> entityClass,
            Field idField,
            Field softDeleteField,
            Map<String, Field> filterableFields,
            Set<String> sortableKeys,
            Map<String, String> sortKeyToProperty
    ) {
        this.entityClass = entityClass;
        this.idField = idField;
        this.softDeleteField = softDeleteField;
        this.filterableFields = filterableFields;
        this.sortableKeys = sortableKeys;
        this.sortKeyToProperty = sortKeyToProperty;
    }

    /**
     * Разбирает класс entity. Вызывать можно и вручную, если нужен только {@link FilterSpecificationBuilder}.
     *
     * @throws IllegalStateException если на entity нет поля с {@link Id}
     */
    public static <M> EntityMetadata<M> of(Class<M> entityClass) {
        Field idField = findIdField(entityClass);
        idField.setAccessible(true);

        Field softDeleteField = findSoftDeleteField(entityClass);
        if (softDeleteField != null) {
            softDeleteField.setAccessible(true);
        }

        Map<String, Field> filterableFields = buildFilterableFields(entityClass);
        Set<String> sortableKeys = new HashSet<>();
        Map<String, String> sortKeyToProperty = new HashMap<>();

        sortableKeys.add(idField.getName());
        sortKeyToProperty.put(idField.getName(), idField.getName());

        for (Field field : filterableFields.values()) {
            FilterFieldAllies annotation = field.getAnnotation(FilterFieldAllies.class);
            String property = resolvePropertyPath(field, annotation);

            registerSortable(sortableKeys, sortKeyToProperty, field.getName(), property);
            if (annotation != null && !annotation.alias().isEmpty()) {
                registerSortable(sortableKeys, sortKeyToProperty, annotation.alias(), property);
            }
        }

        return new EntityMetadata<>(
                entityClass,
                idField,
                softDeleteField,
                Collections.unmodifiableMap(filterableFields),
                Collections.unmodifiableSet(sortableKeys),
                Collections.unmodifiableMap(sortKeyToProperty)
        );
    }

    public Class<M> getEntityClass() {
        return entityClass;
    }

    public Field getIdField() {
        return idField;
    }

    public String getIdFieldName() {
        return idField.getName();
    }

    public boolean hasSoftDelete() {
        return softDeleteField != null;
    }

    public Field getSoftDeleteField() {
        return softDeleteField;
    }

    public String getSoftDeleteFieldName() {
        return softDeleteField != null ? softDeleteField.getName() : null;
    }

    public Map<String, Field> getFilterableFields() {
        return filterableFields;
    }

    public boolean isSortable(String fieldKey) {
        return sortableKeys.contains(fieldKey);
    }

    public String resolveSortProperty(String fieldKey) {
        return sortKeyToProperty.getOrDefault(fieldKey, fieldKey);
    }

    public static int getDefaultMaxPageSize() {
        return DEFAULT_MAX_PAGE_SIZE;
    }

    private static Field findIdField(Class<?> entityClass) {
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new IllegalStateException("No @Id field found on entity " + entityClass.getName());
    }

    private static Field findSoftDeleteField(Class<?> entityClass) {
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(SoftDeleteFlag.class)) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private static <T> Map<String, Field> buildFilterableFields(Class<T> entityClass) {
        Map<String, Field> result = new HashMap<>();
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(FilterFieldAllies.class)) {
                    continue;
                }
                result.putIfAbsent(field.getName(), field);
                FilterFieldAllies annotation = field.getAnnotation(FilterFieldAllies.class);
                if (annotation != null && !annotation.alias().isEmpty()) {
                    result.putIfAbsent(annotation.alias(), field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return result;
    }

    private static String resolvePropertyPath(Field field, FilterFieldAllies annotation) {
        if (annotation != null && !annotation.value().isEmpty()) {
            return annotation.value();
        }
        return field.getName();
    }

    private static void registerSortable(
            Set<String> sortableKeys,
            Map<String, String> sortKeyToProperty,
            String key,
            String property
    ) {
        sortableKeys.add(key);
        sortKeyToProperty.put(key, property);
    }
}
