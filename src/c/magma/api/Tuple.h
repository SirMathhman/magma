import java.util.function.BiFunction;
import java.util.function.Function;
public struct Tuple<A, B>(A left, B right) {
	<A, B, C>((Tuple<A, B>) => C) merge=<A, B, C>((Tuple<A, B>) => C) merge(((A, B) => C) merger){
		return tuple ->merger.apply(tuple.left, tuple.right);
	};
}