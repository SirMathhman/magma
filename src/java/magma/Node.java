package magma;

import magma.option.Option;
import magma.option.Some;

import java.util.HashMap;
import java.util.Map;

public record Node(
        Option<String> type,
        Map<String, Integer> integers,
        Map<String, String> strings
) {
    public Node(String type) {
        this(new Some<>(type), new HashMap<>(), new HashMap<>());
    }

    public Node withInt(String propertyKey, int propertyValue) {
        integers.put(propertyKey, propertyValue);
        return this;
    }

    public Node withString(String propertyKey, String propertyValue) {
        strings.put(propertyKey, propertyValue);
        return this;
    }
}
