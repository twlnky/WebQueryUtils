package nips.dev.springqueryutils.support;

import nips.dev.springqueryutils.dto.EntityDtoMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestItemDtoMapper extends EntityDtoMapper<TestItem, TestItemDto> {

    @Override
    TestItemDto toDto(TestItem model);

    @Override
    TestItem toModel(TestItemDto dto);

    @Override
    default Class<TestItem> modelClass() {
        return TestItem.class;
    }

    @Override
    default Class<TestItemDto> dtoClass() {
        return TestItemDto.class;
    }
}
