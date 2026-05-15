package nips.dev.springqueryutils.dto;

import nips.dev.springqueryutils.exсeption.ValidationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DtoMapper {

    private final Map<MappingKey, EntityDtoMapper<?, ?>> mappers;

    public DtoMapper(List<EntityDtoMapper<?, ?>> mapperList) {
        Map<MappingKey, EntityDtoMapper<?, ?>> registry = new HashMap<>();
        for (EntityDtoMapper<?, ?> mapper : mapperList) {
            MappingKey key = new MappingKey(mapper.modelClass(), mapper.dtoClass());
            if (registry.put(key, mapper) != null) {
                throw new IllegalStateException(
                        "Duplicate mapper for " + mapper.modelClass().getName() + " -> " + mapper.dtoClass().getName()
                );
            }
        }
        this.mappers = Map.copyOf(registry);
    }

    @SuppressWarnings("unchecked")
    public <M, D> D toDto(M model, Class<D> dtoClass) {
        if (model == null) {
            return null;
        }
        EntityDtoMapper<M, D> mapper = resolveMapper((Class<M>) model.getClass(), dtoClass);
        return mapper.toDto(model);
    }

    @SuppressWarnings("unchecked")
    public <M, D> List<D> toDto(List<M> models, Class<D> dtoClass) {
        if (models == null) {
            return List.of();
        }
        Class<M> modelClass = (Class<M>) resolveListElementType(models);
        EntityDtoMapper<M, D> mapper = resolveMapper(modelClass, dtoClass);
        return models.stream().map(mapper::toDto).toList();
    }

    @SuppressWarnings("unchecked")
    public <M, D> M toModel(D dto, Class<M> modelClass) {
        if (dto == null) {
            return null;
        }
        EntityDtoMapper<M, D> mapper = resolveMapper(modelClass, (Class<D>) dto.getClass());
        return mapper.toModel(dto);
    }

    private <M, D> EntityDtoMapper<M, D> resolveMapper(Class<M> modelClass, Class<D> dtoClass) {
        EntityDtoMapper<?, ?> mapper = mappers.get(new MappingKey(modelClass, dtoClass));
        if (mapper == null) {
            throw new ValidationException(
                    "No MapStruct mapper registered for " + modelClass.getName() + " -> " + dtoClass.getName()
                            + ". Define @Mapper(componentModel = \"spring\") implementing EntityDtoMapper."
            );
        }
        @SuppressWarnings("unchecked")
        EntityDtoMapper<M, D> typedMapper = (EntityDtoMapper<M, D>) mapper;
        return typedMapper;
    }

    private static Class<?> resolveListElementType(List<?> models) {
        if (models.isEmpty()) {
            throw new ValidationException("Cannot infer model type from an empty list");
        }
        return models.getFirst().getClass();
    }

    private record MappingKey(Class<?> modelClass, Class<?> dtoClass) {
    }
}
