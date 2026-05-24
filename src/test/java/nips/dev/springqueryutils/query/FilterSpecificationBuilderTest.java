package nips.dev.springqueryutils.query;

import nips.dev.springqueryutils.exсeption.ValidationException;
import nips.dev.springqueryutils.support.TestItem;
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
class FilterSpecificationBuilderTest {

    @Autowired
    private TestItemService service;

    @Autowired
    private TestItemRepository repository;

    @Test
    void rejectsInvalidFilterFormat() {
        Filter filter = new Filter();
        filter.getFilter().add("onlyOnePart");

        assertThrows(ValidationException.class, () -> FilterSpecificationBuilder.build(filter, TestItem.class));
    }

    @Test
    void rejectsUnknownFilterField() {
        Filter filter = new Filter();
        filter.getFilter().add("unknown:EQUALS:x");

        assertThrows(ValidationException.class, () -> FilterSpecificationBuilder.build(filter, TestItem.class));
    }

    @Test
    void rejectsUnknownOperator() {
        Filter filter = new Filter();
        filter.getFilter().add("score:UNKNOWN:1");

        assertThrows(ValidationException.class, () -> FilterSpecificationBuilder.build(filter, TestItem.class));
    }

    @Test
    void filtersByAliasAndOperatorThroughService() {
        TestItem alpha = save("alpha-one", 10);
        save("beta-two", 5);

        Filter filter = new Filter();
        filter.getFilter().add("itemName:LIKE:alpha");
        filter.getFilter().add("score:GREATER:7");

        var result = service.list(filter, new Pagination(0, 20), null);

        assertThat(result.getData()).extracting(TestItem::getId).containsExactly(alpha.getId());
    }

    private TestItem save(String name, int score) {
        TestItem item = new TestItem();
        item.setName(name);
        item.setScore(score);
        return repository.save(item);
    }
}
