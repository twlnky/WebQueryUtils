package nips.dev.springqueryutils.dto;

public interface EntityDtoMapper<M, D> {

    Class<M> modelClass();

    Class<D> dtoClass();

    D toDto(M model);

    M toModel(D dto);
}
