package magma;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Node {
    private final Map<String, Integer> integers;

    public Node() {
        this(new HashMap<>());
    }

    public Node(Map<String, Integer> integers) {
        this.integers = integers;
    }

    public Node withInt(String propertyKey, int propertyValue) {
        integers.put(propertyKey, propertyValue);
        return this;
    }

    public Optional<Integer> ordinal() {
        return Optional.ofNullable(integers.get("ordinal"));
    }

    public Optional<Integer> addressOrValue() {
        return Optional.ofNullable(integers.get("addressOrValue"));
    }
}