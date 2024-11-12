package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.result.Result;
import magma.app.compile.error.CompileError;
import magma.java.JavaList;
import magma.java.JavaStreams;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Node {
    default magma.api.stream.Stream<Tuple<String, List<Node>>> streamNodeLists() {
        return JavaStreams.fromList(streamNodeListsToNativeStream().toList());
    }

    default magma.api.stream.Stream<Tuple<String, Node>> streamNodes() {
        return JavaStreams.fromList(streamNodesToNativeStream().toList());
    }

    Option<String> findString(String propertyKey);

    String asString();

    String format(int depth);

    Option<JavaList<Node>> findNodeList(String propertyKey);

    Node withNodeList(String propertyKey, List<Node> propertyValues);

    Node withString(String propertyKey, String propertyValue);

    Node retype(String type);

    boolean is(String type);

    Option<String> findType();

    Option<Node> merge(Node other);

    boolean isTyped();

    Stream<Tuple<String, String>> streamStrings();

    Stream<Tuple<String, List<Node>>> streamNodeListsToNativeStream();

    Node withNode(String propertyKey, Node propertyValue);

    Option<Node> findNode(String propertyKey);

    Stream<Tuple<String, Node>> streamNodesToNativeStream();

    Option<Result<Node, CompileError>> mapNodeList(String propertyKey, Function<List<Node>, Result<List<Node>, CompileError>> mapper);

    Option<Result<Node, CompileError>> mapNode(String propertyKey, Function<Node, Result<Node, CompileError>> mapper);

    Option<Result<Node, CompileError>> mapString(String propertyKey, Function<String, Result<String, CompileError>> mapper);

    boolean hasString(String propertyKey);

    Node withInt(String propertyKey, int propertyValue);

    Option<Integer> findInt(String propertyKey);

    boolean hasInteger(String propertyKey);
}
