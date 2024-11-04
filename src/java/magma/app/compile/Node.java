package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Node {
    Option<String> findString(String propertyKey);

    String asString();

    String format(int depth);

    Option<List<Node>> findNodeList(String propertyKey);

    Node withNodeList(String propertyKey, List<Node> propertyValues);

    Node withString(String propertyKey, String propertyValue);

    Option<Node> retype(String type);

    boolean is(String type);

    Option<String> findType();

    Option<Node> merge(Node other);

    boolean isTyped();

    Stream<Tuple<String, String>> streamStrings();

    Stream<Tuple<String, List<Node>>> streamNodeLists();

    Node withNode(String propertyKey, Node propertyValue);

    Option<Node> findNode(String propertyKey);

    Stream<Tuple<String, Node>> streamNodes();

    Option<Result<Node, CompileError>> mapNodeList(String propertyKey, Function<List<Node>, Result<List<Node>, CompileError>> mapper);

    Option<Result<Node, CompileError>> mapNode(String propertyKey, Function<Node, Result<Node, CompileError>> mapper);

    Option<Result<Node, CompileError>> mapString(String propertyKey, Function<String, Result<String, CompileError>> mapper);
}
