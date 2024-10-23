package magma.app.compile;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class MapNodes {
    static MapNode merge0(MapNode self, Node other) {
        final var stringsCopy = new HashMap<String, String>();
        Stream.concat(self.streamStrings(), other.streamStrings()).forEach(tuple -> stringsCopy.put(tuple.left(), tuple.right()));

        final var stringListCopy = new HashMap<String, List<String>>();
        Stream.concat(self.streamStringLists(), other.streamStringLists()).forEach(tuple -> stringListCopy.put(tuple.left(), tuple.right()));

        final var nodesCopy = new HashMap<String, Node>();
        Stream.concat(self.streamNodes(), other.streamNodes()).forEach(tuple -> nodesCopy.put(tuple.left(), tuple.right()));

        final var nodeListCopy = new HashMap<String, List<Node>>();
        Stream.concat(self.streamNodeLists(), other.streamNodeLists()).forEach(tuple -> nodeListCopy.put(tuple.left(), tuple.right()));

        return new MapNode(self.findType(), stringsCopy, stringListCopy, nodesCopy, nodeListCopy);
    }
}
