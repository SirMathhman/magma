#include "../../../magma/api/Tuple.h"
#include "../../../magma/api/option/None.h"
#include "../../../magma/api/option/Option.h"
#include "../../../magma/api/option/Some.h"
#include "../../../java/util/Optional.h"
#include "../../../java/util/function/Function.h"
#include "../../../java/util/function/Supplier.h"
struct Err<T, X>(X error) implements Result<T, X>{
	<R>Result<R, X> flatMapValue(Function<T, Result<R, X>> mapper){
		return new Err<>(this.error);
	}
	<R>Result<R, X> mapValue(Function<T, R> mapper){
		return new Err<>(this.error);
	}
	<R>Result<T, R> mapErr(Function<X, R> mapper){
		return new Err<>(mapper.apply(this.error));
	}
	<R>R match(Function<T, R> onOk, Function<X, R> onErr){
		return onErr.apply(this.error);
	}
	<R>Result<Tuple<T, R>, X> and(Supplier<Result<R, X>> other){
		return new Err<>(this.error);
	}
	<R>Result<T, Tuple<X, R>> or(Supplier<Result<T, R>> other){
		return other.get().mapErr(()->new Tuple<>(this.error, otherErr));
	}
	boolean isOk(){
		return false;
	}
	Optional<X> findError0(){
		return Optional.of(this.error);
	}
	Option<X> findError(){
		return findError0().<Option<X>>map(Some::new).orElseGet(None::new);
	}
}
