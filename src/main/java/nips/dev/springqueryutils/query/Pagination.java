package nips.dev.springqueryutils.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    private int page = 0;
    private int size = 20;

}

