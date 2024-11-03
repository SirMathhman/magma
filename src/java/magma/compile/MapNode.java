package magma.compile;

import magma.option.None;
import magma.option.Option;
import magma.option.Some;

import java.util.Map;

public record MapNode(Map<String, String> strings) implements Node {
    @Override
    public Option<String> findString(String propertyKey) {
        if (strings().containsKey(propertyKey)) {
            return new Some<>(strings().get(propertyKey));
        } else {
            return new None<>();
        }
    }
}