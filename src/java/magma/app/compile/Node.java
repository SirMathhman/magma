package magma.app.compile;

import magma.option.Option;

public interface Node {
    Option<String> findString(String propertyKey);

    String asString();
}
