package magma.app.compile;

import magma.api.Tuple;
import magma.api.option.Option;
import magma.api.stream.Stream;
import magma.api.Display;
import magma.java.JavaList;
import magma.java.JavaMap;

import java.util.function.Function;

public interface Node extends Display {
    Node withInt(String propertyKey, int propertyValue);

    Node withString(String propertyKey, String propertyValue);

    Node withNodeList(String propertyKey, JavaList<Node> propertyValues);

    String format(int depth);

    <T> String join(JavaMap<String, T> map, int depth, Function<T, String> formatter);

    boolean is(String type);

    Option<String> findString(String propertyKey);

    Option<JavaList<Node>> findNodeList(String propertyKey);

    Option<Integer> findInt(String propertyKey);

    @Override
    String display();

    Node retype(String type);

    Stream<Tuple<String, JavaList<Node>>> streamNodeLists();

    Stream<Tuple<String, Node>> streamNodes();

    Stream<Tuple<String, String>> streamStrings();

    Stream<Tuple<String, Integer>> streamIntegers();

    Node merge(Node other);

    Node withNode(String propertyKey, Node propertyValue);

    Option<Node> findNode(String propertyKey);
}
