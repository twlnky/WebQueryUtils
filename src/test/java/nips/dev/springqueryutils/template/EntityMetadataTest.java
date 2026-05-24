package nips.dev.springqueryutils.template;

import nips.dev.springqueryutils.support.TestItem;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntityMetadataTest {

    @Test
    void readsIdSoftDeleteAndFilterableFields() {
        EntityMetadata<TestItem> metadata = EntityMetadata.of(TestItem.class);

        assertThat(metadata.getIdFieldName()).isEqualTo("id");
        assertThat(metadata.hasSoftDelete()).isTrue();
        assertThat(metadata.getSoftDeleteFieldName()).isEqualTo("deleted");
        assertThat(metadata.getFilterableFields()).containsKeys("name", "itemName", "score");
        assertThat(metadata.isSortable("id")).isTrue();
        assertThat(metadata.isSortable("itemName")).isTrue();
        assertThat(metadata.isSortable("unknown")).isFalse();
        assertThat(metadata.resolveSortProperty("itemName")).isEqualTo("name");
    }
}
