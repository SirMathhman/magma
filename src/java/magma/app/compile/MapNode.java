package magma.app.compile;

import magma.api.option.None;
import magma.api.option.Option;
import magma.api.option.Some;

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

    @Override
    public String asString() {
        /*
        TODO: this is a stub for now
         */
        return toString();
    }
}