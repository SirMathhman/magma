package magma.app.compile;

import magma.api.option.Option;

import java.util.List;

public interface Node {
    Option<String> findString(String propertyKey);

    String asString();

    Option<List<Node>> findNodeList(String propertyKey);

    Option<Node> withNodeList(String propertyKey, List<Node> propertyValues);

    Option<Node> withString(String propertyKey, String propertyValue);

    Option<Node> retype(String type);

    boolean is(String type);
}
