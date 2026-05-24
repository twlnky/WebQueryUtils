package nips.dev.springqueryutils.dto;

/**
 * Маркер для MapStruct: {@link DtoMapper} по нему понимает, какой mapper вызвать.
 *
 * <p>Пример: {@code ItemDtoMapper extends EntityDtoMapper<Item, ItemDto>} плюс
 * {@code default Class<Item> modelClass() { return Item.class; }}.
 *
 * @param <M> entity
 * @param <D> DTO
 * @author nip
 * @since 0.0.1
 */
public interface EntityDtoMapper<M, D> {

    Class<M> modelClass();

    Class<D> dtoClass();

    D toDto(M model);

    M toModel(D dto);
}
