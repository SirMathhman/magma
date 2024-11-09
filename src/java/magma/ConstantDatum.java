package magma;

import java.util.Collections;
import java.util.List;

public record ConstantDatum(long value) implements Datum {
    @Override
    public List<Long> list() {
        return Collections.singletonList(value);
    }
}
