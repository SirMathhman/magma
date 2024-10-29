package magma.compile;

import magma.core.String_;
import magma.core.option.Option;

public interface Node {
    Option<String_> find(String propertyKey);

    Option<Node> withString(String propertyKey, String_ propertyValue);

    String_ format();
}
