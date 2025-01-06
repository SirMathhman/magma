#include "temp.h";
#include "temp.h";
#include "temp.h";
#include "temp.h";
struct Err<T, X>(X error) {
	void findValue(void* __ref__){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return {
			void __caller__ = Optional.empty;
			__caller__.()
		};
	}
	void findError(void* __ref__){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return {
			void __caller__ = Optional.of;
			__caller__.(this.error)
		};
	}
	void and(void* __ref__, Supplier<Result<R, X>> otherSupplier){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return temp();
	}
	void mapValue(void* __ref__, Function<T, R> mapper){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return temp();
	}
	void flatMapValue(void* __ref__, Function<T, Result<R, X>> mapper){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return temp();
	}
	void match(void* __ref__, Function<T, R> onOk, Function<X, R> onErr){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return {
			void __caller__ = onErr.apply;
			__caller__.(this.error)
		};
	}
	void mapErr(void* __ref__, Function<X, R> mapper){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return temp();
	}
	void isOk(void* __ref__){
		struct Err<T, X>(X error) this = *(struct Err<T, X>(X error)*) __ref__;
		return false;
	}
};