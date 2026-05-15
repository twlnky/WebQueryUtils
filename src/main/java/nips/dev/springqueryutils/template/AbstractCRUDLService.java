package nips.dev.springqueryutils.template;

import jakarta.persistence.EntityManager;
import nips.dev.springqueryutils.dto.DtoMapper;
import nips.dev.springqueryutils.exсeption.ResourceNotFoundException;
import nips.dev.springqueryutils.exсeption.ValidationException;
import nips.dev.springqueryutils.query.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class AbstractCRUDLService<M, ID, R extends JpaRepository<M, ID> & JpaSpecificationExecutor<M>> {

    protected final R repository;
    protected final DtoMapper dtoMapper;
    protected final EntityManager entityManager;
    protected final Class<M> modelClass;
    private final EntityMetadata<M> metadata;

    protected AbstractCRUDLService(R repository, DtoMapper dtoMapper, EntityManager entityManager, Class<M> modelClass) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.entityManager = entityManager;
        this.modelClass = modelClass;
        this.metadata = EntityMetadata.of(modelClass);
    }

    @Transactional
    public M create(M model) {
        return repository.save(model);
    }

    @Transactional
    public <D> D create(M model, Class<D> dtoClass) {
        M saved = create(model);
        return dtoMapper.toDto(saved, dtoClass);
    }

    @Transactional(readOnly = true)
    public M getById(ID id) {
        return findActiveById(id);
    }

    @Transactional(readOnly = true)
    public <D> D getById(ID id, Class<D> dtoClass) {
        M model = getById(id);
        return dtoMapper.toDto(model, dtoClass);
    }

    @Transactional
    public M update(ID id, M model) {
        findActiveById(id);
        setModelId(model, id);
        return repository.save(model);
    }

    @Transactional
    public <D> D update(ID id, M model, Class<D> dtoClass) {
        M updated = update(id, model);
        return dtoMapper.toDto(updated, dtoClass);
    }

    @Transactional
    public void delete(ID id) {
        if (metadata.hasSoftDelete()) {
            softDelete(id);
        } else {
            if (!repository.existsById(id)) {
                throw new ResourceNotFoundException(getResourceName(), id);
            }
            repository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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
        Specification<M> spec = FilterSpecificationBuilder.build(filter, metadata);

        if (metadata.hasSoftDelete()) {
            Specification<M> softDeleteSpec = (root, query, cb) ->
                    cb.equal(root.get(metadata.getSoftDeleteFieldName()), false);
            spec = spec == null ? softDeleteSpec : spec.and(softDeleteSpec);
        }

        return spec;
    }

    protected Pageable buildPageable(Pagination pagination, Sort sort) {
        if (pagination == null) {
            pagination = new Pagination();
        }

        int page = pagination.getPage();
        int size = pagination.getSize();
        if (page < 0) {
            throw new ValidationException("Page must be >= 0");
        }
        if (size < 1) {
            throw new ValidationException("Page size must be >= 1");
        }
        if (size > EntityMetadata.getDefaultMaxPageSize()) {
            throw new ValidationException(
                    "Page size must not exceed " + EntityMetadata.getDefaultMaxPageSize()
            );
        }

        org.springframework.data.domain.Sort springSort = org.springframework.data.domain.Sort.unsorted();
        if (sort != null && sort.getField() != null && !sort.getField().isEmpty()) {
            if (!metadata.isSortable(sort.getField())) {
                throw new ValidationException("Field '" + sort.getField() + "' is not sortable");
            }
            org.springframework.data.domain.Sort.Direction direction = sort.getDirection() == Sort.SortDirection.ASC
                    ? org.springframework.data.domain.Sort.Direction.ASC
                    : org.springframework.data.domain.Sort.Direction.DESC;
            String sortProperty = metadata.resolveSortProperty(sort.getField());
            springSort = org.springframework.data.domain.Sort.by(direction, sortProperty);
        }

        return PageRequest.of(page, size, springSort);
    }

    protected void softDelete(ID id) {
        M model = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceName(), id));
        try {
            if (Boolean.TRUE.equals(metadata.getSoftDeleteField().get(model))) {
                return;
            }
            metadata.getSoftDeleteField().set(model, true);
            repository.save(model);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to soft delete entity", e);
        }
    }

    protected void setModelId(M model, ID id) {
        try {
            metadata.getIdField().set(model, id);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set entity id", e);
        }
    }

    protected M findActiveById(ID id) {
        Specification<M> idSpec = (root, query, cb) ->
                cb.equal(root.get(metadata.getIdFieldName()), id);

        Specification<M> spec = idSpec;
        if (metadata.hasSoftDelete()) {
            Specification<M> activeSpec = (root, query, cb) ->
                    cb.equal(root.get(metadata.getSoftDeleteFieldName()), false);
            spec = spec.and(activeSpec);
        }

        return repository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException(getResourceName(), id));
    }

    protected String getResourceName() {
        return modelClass.getSimpleName();
    }

    protected EntityMetadata<M> getMetadata() {
        return metadata;
    }
}
