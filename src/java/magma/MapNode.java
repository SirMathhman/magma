package magma;

import java.util.Map;

public record MapNode(Map<String, String> strings) {
    Option<String> findString(String propertyKey) {
        if (strings().containsKey(propertyKey)) {
            return new Some<>(strings().get(propertyKey));
        } else {
            return new None<>();
        }
    }
}