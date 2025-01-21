import java.util.function.BiFunction;
import java.util.function.Function;

 <A, B, C>Function<Tuple<A, B>, C> merge(BiFunction<A, B, C> merger){
	return auto _lambda7_(auto tuple){
		return merger.apply(tuple.left, tuple.right);
	};
}

<R>Tuple<A, R> mapRight(Function<B, R> mapper){
	return new Tuple<>(this.left, mapper.apply(this.right));
}
struct Tuple<A, B>(A left, B right) {
}

