package nips.dev.springqueryutils.dto;

import nips.dev.springqueryutils.exсeption.ValidationException;
import nips.dev.springqueryutils.support.TestItem;
import nips.dev.springqueryutils.support.TestItemDto;
import nips.dev.springqueryutils.support.TestItemDtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DtoMapperTest {

    @Autowired
    private DtoMapper dtoMapper;

    @Autowired
    private TestItemDtoMapper testItemDtoMapper;

    @Test
    void mapsEntityToDtoAndBack() {
        TestItem item = new TestItem();
        item.setName("mapped");
        item.setScore(42);

        TestItemDto dto = dtoMapper.toDto(item, TestItemDto.class);
        assertThat(dto.getName()).isEqualTo("mapped");
        assertThat(dto.getScore()).isEqualTo(42);

        TestItem model = dtoMapper.toModel(dto, TestItem.class);
        assertThat(model.getName()).isEqualTo("mapped");
        assertThat(model.getScore()).isEqualTo(42);
    }

    @Test
    void mapsEntityListToDtoList() {
        TestItem first = new TestItem();
        first.setName("a");
        first.setScore(1);
        TestItem second = new TestItem();
        second.setName("b");
        second.setScore(2);

        List<TestItemDto> dtos = dtoMapper.toDto(List.of(first, second), TestItemDto.class);

        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting(TestItemDto::getName).containsExactly("a", "b");
    }

    @Test
    void throwsWhenMapperMissing() {
        assertThrows(ValidationException.class, () -> dtoMapper.toDto(new TestItem(), String.class));
    }

    @Test
    void rejectsDuplicateMapperRegistration() {
        assertThrows(
                IllegalStateException.class,
                () -> new DtoMapper(List.of(testItemDtoMapper, testItemDtoMapper))
        );
    }
}
