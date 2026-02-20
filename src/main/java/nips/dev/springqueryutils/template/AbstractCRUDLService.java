package nips.dev.springqueryutils.template;

import jakarta.persistence.EntityManager;
import nips.dev.springqueryutils.annotatons.SoftDeleteFlag;
import nips.dev.springqueryutils.dto.DtoMapper;
import nips.dev.springqueryutils.exсeption.ResourceNotFoundException;
import nips.dev.springqueryutils.query.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.lang.reflect.Field;
import java.util.List;

public abstract class AbstractCRUDLService<M, ID, R extends JpaRepository<M, ID> & JpaSpecificationExecutor<M>> {

    protected final R repository;
    protected final DtoMapper dtoMapper;
    protected final EntityManager entityManager;
    protected final Class<M> modelClass;

    protected AbstractCRUDLService(R repository, DtoMapper dtoMapper, EntityManager entityManager, Class<M> modelClass) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.entityManager = entityManager;
        this.modelClass = modelClass;
    }

    public M create(M model) {
        return repository.save(model);
    }

    public <D> D create(M model, Class<D> dtoClass) {
        M saved = create(model);
        return dtoMapper.toDto(saved, dtoClass);
    }

    public M getById(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceName(), id));
    }

    public <D> D getById(ID id, Class<D> dtoClass) {
        M model = getById(id);
        return dtoMapper.toDto(model, dtoClass);
    }

    public M update(ID id, M model) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(getResourceName(), id);
        }
        setModelId(model, id);
        return repository.save(model);
    }

    public <D> D update(ID id, M model, Class<D> dtoClass) {
        M updated = update(id, model);
        return dtoMapper.toDto(updated, dtoClass);
    }

    public void delete(ID id) {
        if (hasSoftDelete()) {
            softDelete(id);
        } else {
            repository.deleteById(id);
        }
    }

    public PageableResult<List<M>> list(Filter filter, Pagination pagination, Sort sort) {
        Specification<M> spec = buildSpecification(filter);
        Pageable pageable = buildPageable(pagination, sort);
        var page = repository.findAll(spec, pageable);
        
        return PageableResult.of(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    public <D> PageableResult<List<D>> list(Filter filter, Pagination pagination, Sort sort, Class<D> dtoClass) {
        PageableResult<List<M>> result = list(filter, pagination, sort);
        List<D> dtoList = dtoMapper.toDto(result.getData(), dtoClass);
        return new PageableResult<>(
                dtoList,
                result.getTotal(),
                result.getPage(),
                result.getSize(),
                result.getTotalPages(),
                result.isHasNext(),
                result.isHasPrevious()
        );
    }

    protected Specification<M> buildSpecification(Filter filter) {
        Specification<M> spec = FilterSpecificationBuilder.build(filter, modelClass);
        
        if (hasSoftDelete()) {
            Specification<M> softDeleteSpec = (root, query, cb) -> 
                    cb.equal(root.get(getSoftDeleteFieldName()), false);
            spec = spec == null ? softDeleteSpec : spec.and(softDeleteSpec);
        }
        
        return spec;
    }

    protected Pageable buildPageable(Pagination pagination, Sort sort) {
        if (pagination == null) {
            pagination = new Pagination();
        }

        org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
        if (sort != null && sort.getField() != null && !sort.getField().isEmpty()) {
            org.springframework.data.domain.Sort.Direction direction = sort.getDirection() == Sort.SortDirection.ASC
                    ? org.springframework.data.domain.Sort.Direction.ASC
                    : org.springframework.data.domain.Sort.Direction.DESC;
            springSort = org.springframework.data.domain.Sort.by(direction, sort.getField());
        }

        return PageRequest.of(pagination.getPage(), pagination.getSize(), springSort);
    }

    protected boolean hasSoftDelete() {
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(SoftDeleteFlag.class)) {
                return true;
            }
        }
        Class<?> superclass = modelClass.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            for (Field field : superclass.getDeclaredFields()) {
                if (field.isAnnotationPresent(SoftDeleteFlag.class)) {
                    return true;
                }
            }
            superclass = superclass.getSuperclass();
        }
        return false;
    }

    protected String getSoftDeleteFieldName() {
        for (Field field : modelClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(SoftDeleteFlag.class)) {
                return field.getName();
            }
        }
        Class<?> superclass = modelClass.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            for (Field field : superclass.getDeclaredFields()) {
                if (field.isAnnotationPresent(SoftDeleteFlag.class)) {
                    return field.getName();
                }
            }
            superclass = superclass.getSuperclass();
        }
        return "deleted";
    }

    protected void softDelete(ID id) {
        M model = getById(id);
        try {
            Field field = findSoftDeleteField();
            field.setAccessible(true);
            field.set(model, true);
            repository.save(model);
        } catch (Exception e) {
            throw new RuntimeException("Failed to soft delete entity", e);
        }
    }

    protected Field findSoftDeleteField() {
        Class<?> currentClass = modelClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(SoftDeleteFlag.class)) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new IllegalStateException("SoftDeleteFlag field not found");
    }

    protected void setModelId(M model, ID id) {
        try {
            Field idField = findIdField();
            idField.setAccessible(true);
            idField.set(model, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set entity id", e);
        }
    }

    protected Field findIdField() {
        Class<?> currentClass = modelClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.getName().equals("id") || 
                    field.getName().toLowerCase().endsWith("id")) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new IllegalStateException("Id field not found");
    }

    protected String getResourceName() {
        return modelClass.getSimpleName();
    }
}
