package magma.app.compile;

import magma.java.JavaCollectors;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class MapNodes {
    static MapNode merge0(MapNode self, Node other) {
        final var stringsCopy = new HashMap<String, String>();
        Stream.concat(self.streamStrings().collect(JavaCollectors.asList()).stream(), other.streamStrings().collect(JavaCollectors.asList()).stream()).forEach(tuple -> stringsCopy.put(tuple.left(), tuple.right()));

        final var stringListCopy = new HashMap<String, List<String>>();
        Stream.concat(self.streamStringLists().collect(JavaCollectors.asList()).stream(), other.streamStringLists().collect(JavaCollectors.asList()).stream()).forEach(tuple -> stringListCopy.put(tuple.left(), tuple.right()));

        final var nodesCopy = new HashMap<String, Node>();
        Stream.concat(self.streamNodes().collect(JavaCollectors.asList()).stream(), other.streamNodes().collect(JavaCollectors.asList()).stream()).forEach(tuple -> nodesCopy.put(tuple.left(), tuple.right()));

        final var nodeListCopy = new HashMap<String, List<Node>>();
        Stream.concat(self.streamNodeLists().collect(JavaCollectors.asList()).stream(), other.streamNodeLists().collect(JavaCollectors.asList()).stream()).forEach(tuple -> nodeListCopy.put(tuple.left(), tuple.right()));

        return new MapNode(self.findType(), stringsCopy, stringListCopy, nodesCopy, nodeListCopy);
    }
}
