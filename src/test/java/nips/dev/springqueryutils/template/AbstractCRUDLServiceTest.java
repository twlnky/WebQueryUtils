package nips.dev.springqueryutils.template;

import nips.dev.springqueryutils.exсeption.ResourceNotFoundException;
import nips.dev.springqueryutils.exсeption.ValidationException;
import nips.dev.springqueryutils.query.Pagination;
import nips.dev.springqueryutils.query.Sort;
import nips.dev.springqueryutils.support.TestItem;
import nips.dev.springqueryutils.support.TestItemDto;
import nips.dev.springqueryutils.support.TestItemRepository;
import nips.dev.springqueryutils.support.TestItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class AbstractCRUDLServiceTest {

    @Autowired
    private TestItemService service;

    @Autowired
    private TestItemRepository repository;

    private static TestItem item(String name, int score) {
        TestItem item = new TestItem();
        item.setName(name);
        item.setScore(score);
        return item;
    }

    @Test
    void createGetUpdateAndListWithDto() {
        TestItem created = service.create(item("create", 1));
        assertThat(created.getId()).isNotNull();

        TestItem loaded = service.getById(created.getId());
        assertThat(loaded.getName()).isEqualTo("create");

        loaded.setName("updated");
        loaded.setScore(99);
        service.update(created.getId(), loaded);

        TestItemDto dto = service.getById(created.getId(), TestItemDto.class);
        assertThat(dto.getName()).isEqualTo("updated");
        assertThat(dto.getScore()).isEqualTo(99);

        var page = service.list(null, new Pagination(0, 10), null, TestItemDto.class);
        assertThat(page.getData()).hasSize(1);
        assertThat(page.getTotal()).isEqualTo(1);
    }

    @Test
    void softDeleteHidesEntityFromGetAndList() {
        TestItem saved = service.create(item("to-delete", 3));
        service.delete(saved.getId());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(saved.getId()));

        var list = service.list(null, new Pagination(0, 20), null);
        assertThat(list.getData()).isEmpty();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test
    void listSortsByWhitelistedField() {
        TestItem low = service.create(item("low", 1));
        TestItem high = service.create(item("high", 10));

        Sort sort = new Sort("score", Sort.SortDirection.DESC);
        var result = service.list(null, new Pagination(0, 20), sort);

        assertThat(result.getData()).extracting(TestItem::getId).containsExactly(high.getId(), low.getId());
    }

    @Test
    void rejectsInvalidPaginationAndSort() {
        assertThrows(ValidationException.class, () -> service.list(null, new Pagination(-1, 10), null));
        assertThrows(ValidationException.class, () -> service.list(null, new Pagination(0, 0), null));
        assertThrows(ValidationException.class, () -> service.list(null, new Pagination(0, 10), new Sort("bad", Sort.SortDirection.ASC)));
    }
}
