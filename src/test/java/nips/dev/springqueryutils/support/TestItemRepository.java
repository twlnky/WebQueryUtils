package nips.dev.springqueryutils.support;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TestItemRepository extends JpaRepository<TestItem, Long>, JpaSpecificationExecutor<TestItem> {
}
