package nips.dev.springqueryutils.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sort {
    private String field;
    private SortDirection direction = SortDirection.ASC;

    public enum SortDirection {
        ASC, DESC
    }
}
