package nips.dev.springqueryutils.support;

import jakarta.persistence.EntityManager;
import nips.dev.springqueryutils.dto.DtoMapper;
import nips.dev.springqueryutils.template.AbstractCRUDLService;
import org.springframework.stereotype.Service;

@Service
public class TestItemService extends AbstractCRUDLService<TestItem, Long, TestItemRepository> {

    public TestItemService(TestItemRepository repository, DtoMapper dtoMapper, EntityManager entityManager) {
        super(repository, dtoMapper, entityManager, TestItem.class);
    }
}
