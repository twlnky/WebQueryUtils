package nips.dev.springqueryutils.autoconfigure;

import nips.dev.springqueryutils.exсeption.ValidationException;
import nips.dev.springqueryutils.query.Pagination;
import nips.dev.springqueryutils.support.TestItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource(properties = "spring.query-utils.max-page-size=10")
@Transactional
class SpringQueryUtilsPropertiesTest {

    @Autowired
    private TestItemService service;

    @Test
    void maxPageSizeComesFromProperties() {
        assertThrows(ValidationException.class, () -> service.list(null, new Pagination(0, 20), null));
    }
}
