package magma.java;

import java.util.HashMap;
import java.util.Map;

public record JavaMap<K, V>(Map<K, V> map) {

    public JavaMap() {
        this(new HashMap<>());
    }

    public JavaMap<K, V> put(K name, V type) {
        map.put(name, type);
        return this;
    }
}
