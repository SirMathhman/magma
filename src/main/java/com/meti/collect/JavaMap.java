package com.meti.collect;

import com.meti.core.F1;
import com.meti.core.F2;
import com.meti.option.None;
import com.meti.option.Option;
import com.meti.option.Some;
import com.meti.option.Supplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class JavaMap<A, B> {
    private final Map<A, B> map;

    public JavaMap() {
        this(new HashMap<>());
    }

    public JavaMap(Map<A, B> map) {
        this.map = map;
    }

    public Option<B> applyOptionally(A key) {
        return map.containsKey(key)
                ? new Some<>(map.get(key))
                : new None<>();
    }

    public <E extends Exception> JavaMap<A, B> ensure(A key, Supplier<B, E> generator) throws E {
        if (map.containsKey(key)) {
            return this;
        } else {
            var value = generator.get();
            var copy = new HashMap<>(map);
            copy.put(key, value);
            return new JavaMap<>(copy);
        }
    }

    public Set<A> keys() {
        return map.keySet();
    }

    public <C extends Exception> JavaMap<A, B> mapValue(A key, F1<B, B, C> mapper) throws C {
        var copy = new HashMap<>(map);
        if (copy.containsKey(key)) {
            var oldValue = copy.get(key);
            var newValue = mapper.apply(oldValue);
            copy.put(key, newValue);
        }
        return new JavaMap<>(copy);
    }

    public <C, D extends Exception> JavaMap<A, C> mapValues(F2<A, B, C, D> mapper) throws D {
        var outputMap = new HashMap<A, C>();
        for (A aA : getMap().keySet()) {
            var input = mapper.apply(aA, getMap().get(aA));
            outputMap.put(aA, input);
        }
        return new JavaMap<>(outputMap);
    }

    public Map<A, B> getMap() {
        return map;
    }

    public B orElse(A key, B value) {
        return map.getOrDefault(key, value);
    }

    public JavaMap<A, B> put(A key, B value) {
        var copy = new HashMap<>(map);
        copy.put(key, value);
        return new JavaMap<>(copy);
    }
}