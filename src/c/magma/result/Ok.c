#include "magma/Tuple.h";
#include "java/util/Optional.h";
#include "java/util/function/Function.h";
#include "java/util/function/Supplier.h";
struct Ok<T, X>(T value) {
	void findValue(void* __ref__){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return {
			void __caller__ = Optional.of;
			__caller__(__caller__, this.value)
		};
	}
	void findError(void* __ref__){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return {
			void __caller__ = Optional.empty;
			__caller__(__caller__)
		};
	}
	void and(void* __ref__, Supplier<Result<R, X>> otherSupplier){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		caller();
	}
	void mapValue(void* __ref__, Function<T, R> mapper){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return temp();
	}
	void flatMapValue(void* __ref__, Function<T, Result<R, X>> mapper){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return {
			void __caller__ = mapper.apply;
			__caller__(__caller__, this.value)
		};
	}
	void match(void* __ref__, Function<T, R> onOk, Function<X, R> onErr){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return {
			void __caller__ = onOk.apply;
			__caller__(__caller__, this.value)
		};
	}
	void isOk(void* __ref__){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return true;
	}
	void mapErr(void* __ref__, Function<X, R> mapper){
		struct Ok<T, X>(T value) this = *(struct Ok<T, X>(T value)*) __ref__;
		return temp();
	}
};