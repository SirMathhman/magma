package magma;

import java.util.Arrays;
import java.util.List;

public record ArrayDatum(long... values) implements Datum {
    @Override
    public List<Long> list() {
        return Arrays.stream(values)
                .boxed()
                .toList();
    }
}
