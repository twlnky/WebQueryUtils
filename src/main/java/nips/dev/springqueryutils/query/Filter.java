package nips.dev.springqueryutils.query;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Filter {
    private List<String> filter = new ArrayList<>();
}