#include "../../../java/util/function/Function.h"
#include "../../../java/util/function/Supplier.h"
struct Option<T>{
	<R>Option<R> map(Function<T, R> mapper);
	T orElseGet(Supplier<T> other);
}
