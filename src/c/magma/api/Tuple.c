import java.util.function.BiFunction;
import java.util.function.Function;
 struct Tuple<A, B>(A left, B right) {
	static <A, B, C>Function<Tuple<A, B>, C> merge( BiFunction<A, B, C> merger){
		return tuple ->merger.apply(tuple.left, tuple.right);
	}
	 <R>Tuple<A, R> mapRight( Function<B, R> mapper){
		return new Tuple<>(this.left, mapper.apply(this.right));
	}
}

