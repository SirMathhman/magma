import magma.api.Tuple;import java.util.ArrayList;import java.util.List;import java.util.Optional;import java.util.function.BiFunction;import java.util.function.Function;import java.util.function.Predicate;import java.util.function.Supplier;struct InlinePassUnit<T>(
        State state,
        List<Node> cache,
        T value
) implements PassUnit<T> {
}