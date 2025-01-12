import magma.Tuple;
import magma.option.Option;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
struct HeadedStream<T>(Head<T> head) implements Stream<T> {
	Stream<R> map(Function<T, R> mapper){
		return HeadedStream<>(auto _lambda0_(){
			return this.head.next().map(mapper);
		});
	}
	Stream<R> flatMap(Function<T, Stream<R>> mapper){
		return this.<Stream<R>>foldLeft(HeadedStream<>(EmptyHead<>()), auto _lambda1_(auto rHeadedStream, auto other){
			return rHeadedStream.concat((mapper.apply(other)));
		});
	}
	R foldLeft(R initial, BiFunction<R, T, R> folder){
		auto current = initial;
		while (true) {
			auto finalCurrent = current;
			auto next = this.head.next().map(auto _lambda2_(auto inner){
				return folder.apply(finalCurrent, inner);
			}).toTuple(current);
			if (next.left()) {
				current = next.right();
			}
			else {
				return current;
			}
		}
	}
	Option<R> foldLeftWithInit(Function<T, R> initial, BiFunction<R, T, R> folder){
		return this.head.next().map(initial).map(auto _lambda3_(auto next){
			return foldLeft(next, folder);
		});
	}
	Option<T> next(){
		return this.head.next();
	}
	Stream<T> concat(Stream<T> other){
		return HeadedStream<>(auto _lambda4_(){
			return this.head.next().or(other.next);
		});
	}
	Stream<T> filter(Predicate<T> predicate){
		return flatMap(auto _lambda5_(auto value){
			return HeadedStream<>(predicate.test(value)
                ? new SingleHead<>(value)
                : new EmptyHead<>());
		});
	}
	C collect(Collector<T, C> collector){
		return foldLeft(collector.createInitial(), collector.fold);
	}
	R>> extendBy(Function<T, R> mapper){
		return map(auto _lambda6_(auto inner){
			return Tuple<>(inner, mapper.apply(inner));
		});
	}
}