import magma.api.Tuple;import java.util.ArrayList;import java.util.List;import java.util.Optional;struct InlinePassUnit<T>(
        State state,
        List<Node> cache,
        T value
) implements PassUnit<T> {public InlinePassUnit(T value){this(new State(), new ArrayList<>(), value);}@Override
public Optional<PassUnit<T>> filter(Predicate<T> predicate){if(predicate.test(this.value))return Optional.of(this);return Optional.empty();}@Override
public <R>PassUnit<R> withValue(R value){return new InlinePassUnit<>(this.state, this.cache, value);}@Override
public PassUnit<T> enter(){return new InlinePassUnit<>(this.state.enter(), this.cache, this.value);}@Override
public <R>Optional<PassUnit<R>> filterAndMapToValue(Predicate<T> predicate, Function<T, R> mapper){return filterAndSupply(predicate, ()->new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.value)));}private <R>Optional<PassUnit<R>> filterAndSupply(Predicate<T> predicate, Supplier<PassUnit<R>> supplier){return predicate.test(this.value)
                ? Optional.of(supplier.get())
                : Optional.empty();}@Override
public Optional<PassUnit<T>> filterAndMapToCached(Predicate<T> predicate, Function<T, Tuple<List<Node>, T>> mapper){return filterAndSupply(predicate, ()->{final var mapped=mapper.apply(this.value);final var cached=new ArrayList<>(this.cache);cached.addAll(mapped.left());final var right=mapped.right();return new InlinePassUnit<>(this.state, cached, right);});}@Override
public <R>PassUnit<R> flattenNode(BiFunction<State, T, R> mapper){return new InlinePassUnit<>(this.state, this.cache, mapper.apply(this.state, this.value));}@Override
public PassUnit<T> exit(){return new InlinePassUnit<>(this.state.exit(), this.cache, this.value);}@Override
public <R>PassUnit<R> mapValue(Function<T, R> mapper){final var apply=mapper.apply(this.value);return new InlinePassUnit<>(this.state, this.cache, apply);}}