package magma.app.compile;

import magma.api.Tuple;
import magma.java.NativeListCollector;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class MapNodes {
    static MapNode merge0(MapNode self, Node other) {
        final var stringsCopy = new HashMap<String, String>();
        Stream.concat(self.streamStrings().collect(new NativeListCollector<Tuple<String, String>>()).stream(), other.streamStrings().collect(new NativeListCollector<Tuple<String, String>>()).stream()).forEach(tuple -> stringsCopy.put(tuple.left(), tuple.right()));

        final var stringListCopy = new HashMap<String, List<String>>();
        Stream.concat(self.streamStringLists().collect(new NativeListCollector<Tuple<String, List<String>>>()).stream(), other.streamStringLists().collect(new NativeListCollector<Tuple<String, List<String>>>()).stream()).forEach(tuple -> stringListCopy.put(tuple.left(), tuple.right()));

        final var nodesCopy = new HashMap<String, Node>();
        Stream.concat(self.streamNodes().collect(new NativeListCollector<Tuple<String, Node>>()).stream(), other.streamNodes().collect(new NativeListCollector<Tuple<String, Node>>()).stream()).forEach(tuple -> nodesCopy.put(tuple.left(), tuple.right()));

        final var nodeListCopy = new HashMap<String, List<Node>>();
        Stream.concat(self.streamNodeLists().collect(new NativeListCollector<Tuple<String, List<Node>>>()).stream(), other.streamNodeLists().collect(new NativeListCollector<Tuple<String, List<Node>>>()).stream()).forEach(tuple -> nodeListCopy.put(tuple.left(), tuple.right()));

        return new MapNode(self.findType(), stringsCopy, stringListCopy, nodesCopy, nodeListCopy);
    }
}
